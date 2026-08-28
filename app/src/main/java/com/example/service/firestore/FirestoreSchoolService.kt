package com.example.service.firestore

import android.util.Log
import com.example.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

enum class CloudSyncState {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}

data class CloudSyncStatus(
    val state: CloudSyncState = CloudSyncState.IDLE,
    val lastSyncTimeMillis: Long = System.currentTimeMillis(),
    val message: String = "Cloud Ready"
)

class FirestoreSchoolService {

    companion object {
        private const val TAG = "FirestoreSchoolService"
        private const val DEFAULT_SCHOOL_ID = "SCH-KINGSWAY-01"
        private const val COLLECTION_SCHOOLS = "schools"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_CLASSES = "classes"
        private const val COLLECTION_SUBJECTS = "subjects"
        private const val COLLECTION_PROFILES = "profiles"
        private const val COLLECTION_CHAT_ROOMS = "chat_rooms"
        private const val COLLECTION_CHAT_MESSAGES = "chat_messages"
        private const val COLLECTION_ATTENDANCE = "attendance"
        private const val COLLECTION_CBT_SUBMISSIONS = "cbt_submissions"
        private const val COLLECTION_CBT_EXAMS = "cbt_exams"
        private const val COLLECTION_ANNOUNCEMENTS = "announcements"
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseFirestore.getInstance() not available: ${e.message}")
            null
        }
    }

    private fun getSchoolDoc(schoolId: String = DEFAULT_SCHOOL_ID) =
        firestore?.collection(COLLECTION_SCHOOLS)?.document(schoolId)

    // --- User Synchronization (Students, Teachers, Admins, Parents) ---

    suspend fun saveUser(user: SchoolUser, schoolId: String = DEFAULT_SCHOOL_ID): Boolean {
        val schoolDoc = getSchoolDoc(schoolId) ?: return false
        return try {
            val userMap = hashMapOf(
                "id" to user.id,
                "name" to user.name,
                "role" to user.role.name,
                "email" to user.email,
                "phone" to user.phone,
                "passcode" to user.passcode,
                "className" to user.className,
                "assignedSubjects" to user.assignedSubjects,
                "studentChildId" to (user.studentChildId ?: ""),
                "studentChildName" to (user.studentChildName ?: ""),
                "avatarColorHex" to user.avatarColorHex,
                "photoUri" to (user.photoUri ?: ""),
                "gender" to user.gender,
                "dateOfBirth" to user.dateOfBirth,
                "guardianName" to user.guardianName,
                "guardianPhone" to user.guardianPhone,
                "guardianEmail" to user.guardianEmail,
                "residentialAddress" to user.residentialAddress,
                "bloodGroup" to user.bloodGroup,
                "genotype" to user.genotype,
                "admissionDate" to user.admissionDate,
                "stateOfOrigin" to user.stateOfOrigin,
                "updatedAtMillis" to System.currentTimeMillis()
            )

            schoolDoc
                .collection(COLLECTION_USERS)
                .document(user.id)
                .set(userMap, SetOptions.merge())
                .await()

            Log.d(TAG, "Successfully synced user ${user.name} (${user.id}) to Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user to Firestore: ${e.message}", e)
            false
        }
    }

    suspend fun deleteUser(userId: String, schoolId: String = DEFAULT_SCHOOL_ID): Boolean {
        val schoolDoc = getSchoolDoc(schoolId) ?: return false
        return try {
            schoolDoc
                .collection(COLLECTION_USERS)
                .document(userId)
                .delete()
                .await()
            Log.d(TAG, "Deleted user $userId from Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user from Firestore: ${e.message}", e)
            false
        }
    }

    suspend fun syncAllUsersBatch(users: List<SchoolUser>, schoolId: String = DEFAULT_SCHOOL_ID): Boolean {
        val db = firestore ?: return false
        val schoolDoc = getSchoolDoc(schoolId) ?: return false
        return try {
            val batch = db.batch()
            val usersCol = schoolDoc.collection(COLLECTION_USERS)

            users.forEach { user ->
                val userMap = hashMapOf(
                    "id" to user.id,
                    "name" to user.name,
                    "role" to user.role.name,
                    "email" to user.email,
                    "phone" to user.phone,
                    "passcode" to user.passcode,
                    "className" to user.className,
                    "assignedSubjects" to user.assignedSubjects,
                    "studentChildId" to (user.studentChildId ?: ""),
                    "studentChildName" to (user.studentChildName ?: ""),
                    "avatarColorHex" to user.avatarColorHex,
                    "photoUri" to (user.photoUri ?: ""),
                    "gender" to user.gender,
                    "dateOfBirth" to user.dateOfBirth,
                    "guardianName" to user.guardianName,
                    "guardianPhone" to user.guardianPhone,
                    "guardianEmail" to user.guardianEmail,
                    "residentialAddress" to user.residentialAddress,
                    "bloodGroup" to user.bloodGroup,
                    "genotype" to user.genotype,
                    "admissionDate" to user.admissionDate,
                    "stateOfOrigin" to user.stateOfOrigin,
                    "updatedAtMillis" to System.currentTimeMillis()
                )
                val docRef = usersCol.document(user.id)
                batch.set(docRef, userMap, SetOptions.merge())
            }

            batch.commit().await()
            Log.d(TAG, "Successfully synced ${users.size} users to Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing users batch to Firestore: ${e.message}", e)
            false
        }
    }

    fun observeUsers(schoolId: String = DEFAULT_SCHOOL_ID): Flow<List<SchoolUser>> = callbackFlow {
        val schoolDoc = getSchoolDoc(schoolId)
        if (schoolDoc == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val listener = schoolDoc
            .collection(COLLECTION_USERS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Firestore observeUsers error: ${error.message}", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val userList = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.getString("id") ?: doc.id
                            val name = doc.getString("name") ?: ""
                            val roleStr = doc.getString("role") ?: SchoolRole.STUDENT.name
                            val role = try { SchoolRole.valueOf(roleStr) } catch (e: Exception) { SchoolRole.STUDENT }
                            val email = doc.getString("email") ?: ""
                            val phone = doc.getString("phone") ?: ""
                            val passcode = doc.getString("passcode") ?: "1234"
                            val className = doc.getString("className") ?: ""
                            val assignedSubjects = doc.getString("assignedSubjects") ?: ""
                            val studentChildId = doc.getString("studentChildId")?.takeIf { it.isNotBlank() }
                            val studentChildName = doc.getString("studentChildName")?.takeIf { it.isNotBlank() }
                            val avatarColorHex = doc.getString("avatarColorHex") ?: "#1E40AF"
                            val photoUri = doc.getString("photoUri")?.takeIf { it.isNotBlank() }
                            val gender = doc.getString("gender") ?: "Female"
                            val dateOfBirth = doc.getString("dateOfBirth") ?: "2009-05-14"
                            val guardianName = doc.getString("guardianName") ?: ""
                            val guardianPhone = doc.getString("guardianPhone") ?: ""
                            val guardianEmail = doc.getString("guardianEmail") ?: ""
                            val residentialAddress = doc.getString("residentialAddress") ?: ""
                            val bloodGroup = doc.getString("bloodGroup") ?: "O+"
                            val genotype = doc.getString("genotype") ?: "AA"
                            val admissionDate = doc.getString("admissionDate") ?: "2024-09-10"
                            val stateOfOrigin = doc.getString("stateOfOrigin") ?: "Lagos"

                            SchoolUser(
                                id = id,
                                name = name,
                                role = role,
                                email = email,
                                phone = phone,
                                passcode = passcode,
                                className = className,
                                assignedSubjects = assignedSubjects,
                                studentChildId = studentChildId,
                                studentChildName = studentChildName,
                                avatarColorHex = avatarColorHex,
                                photoUri = photoUri,
                                gender = gender,
                                dateOfBirth = dateOfBirth,
                                guardianName = guardianName,
                                guardianPhone = guardianPhone,
                                guardianEmail = guardianEmail,
                                residentialAddress = residentialAddress,
                                bloodGroup = bloodGroup,
                                genotype = genotype,
                                admissionDate = admissionDate,
                                stateOfOrigin = stateOfOrigin
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error mapping user document: ${e.message}", e)
                            null
                        }
                    }
                    trySend(userList)
                }
            }

        awaitClose { listener.remove() }
    }

    // --- Class Structures & Arms Synchronization ---

    suspend fun saveClass(schoolClass: SchoolClass, schoolId: String = DEFAULT_SCHOOL_ID): Boolean {
        val schoolDoc = getSchoolDoc(schoolId) ?: return false
        return try {
            val docId = if (schoolClass.id > 0) schoolClass.id.toString() else schoolClass.name.replace(" ", "_")
            val classMap = hashMapOf(
                "id" to schoolClass.id,
                "name" to schoolClass.name,
                "level" to schoolClass.level,
                "arm" to schoolClass.arm,
                "classTeacherId" to schoolClass.classTeacherId,
                "classTeacherName" to schoolClass.classTeacherName,
                "studentCount" to schoolClass.studentCount,
                "room" to schoolClass.room,
                "updatedAtMillis" to System.currentTimeMillis()
            )

            schoolDoc
                .collection(COLLECTION_CLASSES)
                .document(docId)
                .set(classMap, SetOptions.merge())
                .await()

            Log.d(TAG, "Successfully synced class ${schoolClass.name} to Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving class to Firestore: ${e.message}", e)
            false
        }
    }

    suspend fun deleteClass(classId: Long, className: String, schoolId: String = DEFAULT_SCHOOL_ID): Boolean {
        val schoolDoc = getSchoolDoc(schoolId) ?: return false
        return try {
            val docId = if (classId > 0) classId.toString() else className.replace(" ", "_")
            schoolDoc
                .collection(COLLECTION_CLASSES)
                .document(docId)
                .delete()
                .await()
            Log.d(TAG, "Deleted class $className from Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting class from Firestore: ${e.message}", e)
            false
        }
    }

    suspend fun syncAllClassesBatch(classes: List<SchoolClass>, schoolId: String = DEFAULT_SCHOOL_ID): Boolean {
        val db = firestore ?: return false
        val schoolDoc = getSchoolDoc(schoolId) ?: return false
        return try {
            val batch = db.batch()
            val classesCol = schoolDoc.collection(COLLECTION_CLASSES)

            classes.forEach { c ->
                val docId = if (c.id > 0) c.id.toString() else c.name.replace(" ", "_")
                val classMap = hashMapOf(
                    "id" to c.id,
                    "name" to c.name,
                    "level" to c.level,
                    "arm" to c.arm,
                    "classTeacherId" to c.classTeacherId,
                    "classTeacherName" to c.classTeacherName,
                    "studentCount" to c.studentCount,
                    "room" to c.room,
                    "updatedAtMillis" to System.currentTimeMillis()
                )
                val docRef = classesCol.document(docId)
                batch.set(docRef, classMap, SetOptions.merge())
            }

            batch.commit().await()
            Log.d(TAG, "Successfully synced ${classes.size} classes to Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing classes batch to Firestore: ${e.message}", e)
            false
        }
    }

    fun observeClasses(schoolId: String = DEFAULT_SCHOOL_ID): Flow<List<SchoolClass>> = callbackFlow {
        val schoolDoc = getSchoolDoc(schoolId)
        if (schoolDoc == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val listener = schoolDoc
            .collection(COLLECTION_CLASSES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Firestore observeClasses error: ${error.message}", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val classList = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L
                            val name = doc.getString("name") ?: ""
                            val level = doc.getString("level") ?: ""
                            val arm = doc.getString("arm") ?: ""
                            val classTeacherId = doc.getString("classTeacherId") ?: ""
                            val classTeacherName = doc.getString("classTeacherName") ?: ""
                            val studentCount = doc.getLong("studentCount")?.toInt() ?: 30
                            val room = doc.getString("room") ?: "Main Block"

                            SchoolClass(
                                id = id,
                                name = name,
                                level = level,
                                arm = arm,
                                classTeacherId = classTeacherId,
                                classTeacherName = classTeacherName,
                                studentCount = studentCount,
                                room = room
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error mapping class document: ${e.message}", e)
                            null
                        }
                    }
                    trySend(classList)
                }
            }

        awaitClose { listener.remove() }
    }

    // --- Subjects Synchronization ---

    suspend fun saveSubject(subject: SchoolSubject, schoolId: String = DEFAULT_SCHOOL_ID): Boolean {
        val schoolDoc = getSchoolDoc(schoolId) ?: return false
        return try {
            val docId = if (subject.id > 0) subject.id.toString() else subject.code.replace(" ", "_")
            val subjectMap = hashMapOf(
                "id" to subject.id,
                "name" to subject.name,
                "code" to subject.code,
                "classLevel" to subject.classLevel,
                "teacherId" to subject.teacherId,
                "teacherName" to subject.teacherName,
                "colorHex" to subject.colorHex,
                "periodsPerWeek" to subject.periodsPerWeek,
                "updatedAtMillis" to System.currentTimeMillis()
            )

            schoolDoc
                .collection(COLLECTION_SUBJECTS)
                .document(docId)
                .set(subjectMap, SetOptions.merge())
                .await()

            Log.d(TAG, "Successfully synced subject ${subject.name} to Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving subject to Firestore: ${e.message}", e)
            false
        }
    }

    suspend fun deleteSubject(subjectId: Long, subjectCode: String, schoolId: String = DEFAULT_SCHOOL_ID): Boolean {
        val schoolDoc = getSchoolDoc(schoolId) ?: return false
        return try {
            val docId = if (subjectId > 0) subjectId.toString() else subjectCode.replace(" ", "_")
            schoolDoc
                .collection(COLLECTION_SUBJECTS)
                .document(docId)
                .delete()
                .await()
            Log.d(TAG, "Deleted subject $subjectCode from Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting subject from Firestore: ${e.message}", e)
            false
        }
    }

    suspend fun syncAllSubjectsBatch(subjects: List<SchoolSubject>, schoolId: String = DEFAULT_SCHOOL_ID): Boolean {
        val db = firestore ?: return false
        val schoolDoc = getSchoolDoc(schoolId) ?: return false
        return try {
            val batch = db.batch()
            val subjectsCol = schoolDoc.collection(COLLECTION_SUBJECTS)

            subjects.forEach { s ->
                val docId = if (s.id > 0) s.id.toString() else s.code.replace(" ", "_")
                val subjectMap = hashMapOf(
                    "id" to s.id,
                    "name" to s.name,
                    "code" to s.code,
                    "classLevel" to s.classLevel,
                    "teacherId" to s.teacherId,
                    "teacherName" to s.teacherName,
                    "colorHex" to s.colorHex,
                    "periodsPerWeek" to s.periodsPerWeek,
                    "updatedAtMillis" to System.currentTimeMillis()
                )
                val docRef = subjectsCol.document(docId)
                batch.set(docRef, subjectMap, SetOptions.merge())
            }

            batch.commit().await()
            Log.d(TAG, "Successfully synced ${subjects.size} subjects to Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing subjects batch to Firestore: ${e.message}", e)
            false
        }
    }

    fun observeSubjects(schoolId: String = DEFAULT_SCHOOL_ID): Flow<List<SchoolSubject>> = callbackFlow {
        val schoolDoc = getSchoolDoc(schoolId)
        if (schoolDoc == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val listener = schoolDoc
            .collection(COLLECTION_SUBJECTS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Firestore observeSubjects error: ${error.message}", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val subjectList = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L
                            val name = doc.getString("name") ?: ""
                            val code = doc.getString("code") ?: ""
                            val classLevel = doc.getString("classLevel") ?: ""
                            val teacherId = doc.getString("teacherId") ?: ""
                            val teacherName = doc.getString("teacherName") ?: ""
                            val colorHex = doc.getString("colorHex") ?: "#2563EB"
                            val periodsPerWeek = doc.getLong("periodsPerWeek")?.toInt() ?: 4

                            SchoolSubject(
                                id = id,
                                name = name,
                                code = code,
                                classLevel = classLevel,
                                teacherId = teacherId,
                                teacherName = teacherName,
                                colorHex = colorHex,
                                periodsPerWeek = periodsPerWeek
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error mapping subject document: ${e.message}", e)
                            null
                        }
                    }
                    trySend(subjectList)
                }
            }

        awaitClose { listener.remove() }
    }

    // --- School Profile & Institutional Config ---

    suspend fun saveSchoolProfile(profile: SchoolProfile, schoolId: String = DEFAULT_SCHOOL_ID): Boolean {
        val targetSchoolId = profile.schoolCode.ifBlank { schoolId }
        val schoolDoc = getSchoolDoc(targetSchoolId) ?: return false
        return try {
            val profileMap = hashMapOf(
                "id" to profile.id,
                "schoolCode" to targetSchoolId,
                "schoolName" to profile.schoolName,
                "schoolMotto" to profile.schoolMotto,
                "schoolEmail" to profile.schoolEmail,
                "schoolPhone" to profile.schoolPhone,
                "schoolAddress" to profile.schoolAddress,
                "academicSession" to profile.academicSession,
                "currentTerm" to profile.currentTerm,
                "principalName" to profile.principalName,
                "schoolLogoBadge" to profile.schoolLogoBadge,
                "updatedAtMillis" to System.currentTimeMillis()
            )

            schoolDoc
                .collection(COLLECTION_PROFILES)
                .document("main_profile")
                .set(profileMap, SetOptions.merge())
                .await()

            Log.d(TAG, "Successfully synced school profile for $targetSchoolId to Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving school profile to Firestore: ${e.message}", e)
            false
        }
    }

    fun observeSchoolProfile(schoolId: String = DEFAULT_SCHOOL_ID): Flow<SchoolProfile?> = callbackFlow {
        val schoolDoc = getSchoolDoc(schoolId)
        if (schoolDoc == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val listener = schoolDoc
            .collection(COLLECTION_PROFILES)
            .document("main_profile")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Firestore observeSchoolProfile error: ${error.message}", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        val id = snapshot.getLong("id") ?: 1L
                        val schoolCode = snapshot.getString("schoolCode") ?: schoolId
                        val schoolName = snapshot.getString("schoolName") ?: "Kingsway Model International College"
                        val schoolMotto = snapshot.getString("schoolMotto") ?: "Excellence, Knowledge & Integrity"
                        val schoolEmail = snapshot.getString("schoolEmail") ?: "info@kingswaycollege.edu.ng"
                        val schoolPhone = snapshot.getString("schoolPhone") ?: "+234 803 123 4567"
                        val schoolAddress = snapshot.getString("schoolAddress") ?: "Plot 12, Academic Avenue, Victoria Island, Lagos"
                        val academicSession = snapshot.getString("academicSession") ?: "2025/2026"
                        val currentTerm = snapshot.getString("currentTerm") ?: "1st Term"
                        val principalName = snapshot.getString("principalName") ?: "Dr. C. Adebayo, Ph.D"
                        val schoolLogoBadge = snapshot.getString("schoolLogoBadge") ?: "KMIC"

                        val profile = SchoolProfile(
                            id = id,
                            schoolCode = schoolCode,
                            schoolName = schoolName,
                            schoolMotto = schoolMotto,
                            schoolAddress = schoolAddress,
                            schoolEmail = schoolEmail,
                            schoolPhone = schoolPhone,
                            academicSession = academicSession,
                            currentTerm = currentTerm,
                            principalName = principalName,
                            schoolLogoBadge = schoolLogoBadge
                        )
                        trySend(profile)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error mapping profile document: ${e.message}", e)
                    }
                }
            }

        awaitClose { listener.remove() }
    }

    // --- Moderated Real-Time Chat Rooms & Messaging ---

    suspend fun saveChatRoom(room: ChatRoom, schoolId: String = DEFAULT_SCHOOL_ID): Boolean {
        val schoolDoc = getSchoolDoc(schoolId) ?: return false
        return try {
            val roomMap = hashMapOf(
                "id" to room.id,
                "title" to room.title,
                "description" to room.description,
                "topic" to room.topic,
                "allowedRoles" to room.allowedRoles,
                "targetClass" to room.targetClass,
                "isModerated" to room.isModerated,
                "isMutedForStudents" to room.isMutedForStudents,
                "pinnedNotice" to (room.pinnedNotice ?: ""),
                "pinnedBy" to (room.pinnedBy ?: ""),
                "createdBy" to room.createdBy,
                "creatorRole" to room.creatorRole.name,
                "createdAtMillis" to room.createdAtMillis,
                "colorHex" to room.colorHex,
                "iconName" to room.iconName,
                "memberCount" to room.memberCount,
                "updatedAtMillis" to System.currentTimeMillis()
            )

            schoolDoc
                .collection(COLLECTION_CHAT_ROOMS)
                .document(room.id)
                .set(roomMap, SetOptions.merge())
                .await()

            Log.d(TAG, "Successfully synced chat room ${room.title} (${room.id}) to Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving chat room to Firestore: ${e.message}", e)
            false
        }
    }

    suspend fun deleteChatRoom(roomId: String, schoolId: String = DEFAULT_SCHOOL_ID): Boolean {
        val schoolDoc = getSchoolDoc(schoolId) ?: return false
        return try {
            schoolDoc
                .collection(COLLECTION_CHAT_ROOMS)
                .document(roomId)
                .delete()
                .await()
            Log.d(TAG, "Successfully deleted chat room $roomId from Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting chat room from Firestore: ${e.message}", e)
            false
        }
    }

    suspend fun setRoomStudentMute(roomId: String, isMuted: Boolean, schoolId: String = DEFAULT_SCHOOL_ID): Boolean {
        val schoolDoc = getSchoolDoc(schoolId) ?: return false
        return try {
            schoolDoc
                .collection(COLLECTION_CHAT_ROOMS)
                .document(roomId)
                .update(
                    mapOf(
                        "isMutedForStudents" to isMuted,
                        "updatedAtMillis" to System.currentTimeMillis()
                    )
                )
                .await()
            Log.d(TAG, "Successfully updated room $roomId student mute status to $isMuted")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating room mute status in Firestore: ${e.message}", e)
            false
        }
    }

    suspend fun setRoomPinnedNotice(
        roomId: String,
        notice: String?,
        pinnedBy: String?,
        schoolId: String = DEFAULT_SCHOOL_ID
    ): Boolean {
        val schoolDoc = getSchoolDoc(schoolId) ?: return false
        return try {
            schoolDoc
                .collection(COLLECTION_CHAT_ROOMS)
                .document(roomId)
                .update(
                    mapOf(
                        "pinnedNotice" to (notice ?: ""),
                        "pinnedBy" to (pinnedBy ?: ""),
                        "updatedAtMillis" to System.currentTimeMillis()
                    )
                )
                .await()
            Log.d(TAG, "Successfully updated pinned notice for room $roomId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating pinned notice in Firestore: ${e.message}", e)
            false
        }
    }

    fun observeChatRooms(schoolId: String = DEFAULT_SCHOOL_ID): Flow<List<ChatRoom>> = callbackFlow {
        val schoolDoc = getSchoolDoc(schoolId)
        if (schoolDoc == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val listener = schoolDoc
            .collection(COLLECTION_CHAT_ROOMS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Firestore observeChatRooms error: ${error.message}", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val rooms = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.getString("id") ?: doc.id
                            val title = doc.getString("title") ?: "Room"
                            val description = doc.getString("description") ?: ""
                            val topic = doc.getString("topic") ?: "General"
                            val allowedRoles = doc.getString("allowedRoles") ?: "ALL"
                            val targetClass = doc.getString("targetClass") ?: "ALL"
                            val isModerated = doc.getBoolean("isModerated") ?: true
                            val isMutedForStudents = doc.getBoolean("isMutedForStudents") ?: false
                            val pinnedNotice = doc.getString("pinnedNotice").takeIf { !it.isNullOrBlank() }
                            val pinnedBy = doc.getString("pinnedBy").takeIf { !it.isNullOrBlank() }
                            val createdBy = doc.getString("createdBy") ?: "System"
                            val creatorRole = try {
                                SchoolRole.valueOf(doc.getString("creatorRole") ?: "ADMIN")
                            } catch (e: Exception) {
                                SchoolRole.ADMIN
                            }
                            val createdAtMillis = doc.getLong("createdAtMillis") ?: System.currentTimeMillis()
                            val colorHex = doc.getString("colorHex") ?: "#1E40AF"
                            val iconName = doc.getString("iconName") ?: "Forum"
                            val memberCount = doc.getLong("memberCount")?.toInt() ?: 32

                            ChatRoom(
                                id = id,
                                title = title,
                                description = description,
                                topic = topic,
                                allowedRoles = allowedRoles,
                                targetClass = targetClass,
                                isModerated = isModerated,
                                isMutedForStudents = isMutedForStudents,
                                pinnedNotice = pinnedNotice,
                                pinnedBy = pinnedBy,
                                createdBy = createdBy,
                                creatorRole = creatorRole,
                                createdAtMillis = createdAtMillis,
                                colorHex = colorHex,
                                iconName = iconName,
                                memberCount = memberCount
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error mapping chat room document ${doc.id}: ${e.message}", e)
                            null
                        }
                    }
                    trySend(rooms)
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun saveChatMessage(message: ChatMessage, schoolId: String = DEFAULT_SCHOOL_ID): Boolean {
        val schoolDoc = getSchoolDoc(schoolId) ?: return false
        return try {
            val docId = if (message.id > 0) message.id.toString() else "msg_${System.currentTimeMillis()}_${(1000..9999).random()}"
            val msgMap = hashMapOf(
                "id" to if (message.id > 0) message.id else System.currentTimeMillis(),
                "channelId" to message.channelId,
                "channelName" to message.channelName,
                "senderId" to message.senderId,
                "senderName" to message.senderName,
                "senderRole" to message.senderRole.name,
                "message" to message.message,
                "timestampMillis" to message.timestampMillis,
                "isModerated" to message.isModerated,
                "moderationReason" to (message.moderationReason ?: ""),
                "deletedBy" to (message.deletedBy ?: ""),
                "isPinned" to message.isPinned,
                "replyToMessageId" to (message.replyToMessageId ?: 0L),
                "replyToSender" to (message.replyToSender ?: ""),
                "replyToText" to (message.replyToText ?: ""),
                "senderAvatarColor" to message.senderAvatarColor,
                "senderPhotoUri" to (message.senderPhotoUri ?: "")
            )

            schoolDoc
                .collection(COLLECTION_CHAT_MESSAGES)
                .document(docId)
                .set(msgMap, SetOptions.merge())
                .await()

            Log.d(TAG, "Successfully synced chat message in ${message.channelId} to Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving chat message to Firestore: ${e.message}", e)
            false
        }
    }

    suspend fun moderateChatMessage(
        messageId: Long,
        moderatorName: String,
        reason: String,
        schoolId: String = DEFAULT_SCHOOL_ID
    ): Boolean {
        val schoolDoc = getSchoolDoc(schoolId) ?: return false
        return try {
            // Find message document matching id
            val snapshot = schoolDoc
                .collection(COLLECTION_CHAT_MESSAGES)
                .whereEqualTo("id", messageId)
                .get()
                .await()

            for (doc in snapshot.documents) {
                doc.reference.update(
                    mapOf(
                        "isModerated" to true,
                        "deletedBy" to moderatorName,
                        "moderationReason" to reason
                    )
                ).await()
            }
            Log.d(TAG, "Successfully updated message $messageId moderation in Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error moderating message in Firestore: ${e.message}", e)
            false
        }
    }

    suspend fun unmoderateChatMessage(
        messageId: Long,
        schoolId: String = DEFAULT_SCHOOL_ID
    ): Boolean {
        val schoolDoc = getSchoolDoc(schoolId) ?: return false
        return try {
            val snapshot = schoolDoc
                .collection(COLLECTION_CHAT_MESSAGES)
                .whereEqualTo("id", messageId)
                .get()
                .await()

            for (doc in snapshot.documents) {
                doc.reference.update(
                    mapOf(
                        "isModerated" to false,
                        "deletedBy" to null,
                        "moderationReason" to null
                    )
                ).await()
            }
            Log.d(TAG, "Successfully unmoderated message $messageId in Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error unmoderating message in Firestore: ${e.message}", e)
            false
        }
    }

    suspend fun deleteChatMessage(
        messageId: Long,
        schoolId: String = DEFAULT_SCHOOL_ID
    ): Boolean {
        val schoolDoc = getSchoolDoc(schoolId) ?: return false
        return try {
            val snapshot = schoolDoc
                .collection(COLLECTION_CHAT_MESSAGES)
                .whereEqualTo("id", messageId)
                .get()
                .await()

            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }
            Log.d(TAG, "Successfully deleted message $messageId from Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting message in Firestore: ${e.message}", e)
            false
        }
    }

    fun observeRoomMessages(channelId: String, schoolId: String = DEFAULT_SCHOOL_ID): Flow<List<ChatMessage>> = callbackFlow {
        val schoolDoc = getSchoolDoc(schoolId)
        if (schoolDoc == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val listener = schoolDoc
            .collection(COLLECTION_CHAT_MESSAGES)
            .whereEqualTo("channelId", channelId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Firestore observeRoomMessages error: ${error.message}", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val messages = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.getLong("id") ?: doc.id.hashCode().toLong()
                            val cId = doc.getString("channelId") ?: channelId
                            val channelName = doc.getString("channelName") ?: ""
                            val senderId = doc.getString("senderId") ?: ""
                            val senderName = doc.getString("senderName") ?: "Unknown"
                            val senderRole = try {
                                SchoolRole.valueOf(doc.getString("senderRole") ?: "STUDENT")
                            } catch (e: Exception) {
                                SchoolRole.STUDENT
                            }
                            val message = doc.getString("message") ?: ""
                            val timestampMillis = doc.getLong("timestampMillis") ?: System.currentTimeMillis()
                            val isModerated = doc.getBoolean("isModerated") ?: false
                            val moderationReason = doc.getString("moderationReason").takeIf { !it.isNullOrBlank() }
                            val deletedBy = doc.getString("deletedBy").takeIf { !it.isNullOrBlank() }
                            val isPinned = doc.getBoolean("isPinned") ?: false
                            val replyToMessageId = doc.getLong("replyToMessageId").takeIf { it != null && it > 0 }
                            val replyToSender = doc.getString("replyToSender").takeIf { !it.isNullOrBlank() }
                            val replyToText = doc.getString("replyToText").takeIf { !it.isNullOrBlank() }
                            val senderAvatarColor = doc.getString("senderAvatarColor") ?: "#1E40AF"
                            val senderPhotoUri = doc.getString("senderPhotoUri").takeIf { !it.isNullOrBlank() }

                            ChatMessage(
                                id = id,
                                channelId = cId,
                                channelName = channelName,
                                senderId = senderId,
                                senderName = senderName,
                                senderRole = senderRole,
                                message = message,
                                timestampMillis = timestampMillis,
                                isModerated = isModerated,
                                moderationReason = moderationReason,
                                deletedBy = deletedBy,
                                isPinned = isPinned,
                                replyToMessageId = replyToMessageId,
                                replyToSender = replyToSender,
                                replyToText = replyToText,
                                senderAvatarColor = senderAvatarColor,
                                senderPhotoUri = senderPhotoUri
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error mapping message doc ${doc.id}: ${e.message}", e)
                            null
                        }
                    }.sortedBy { it.timestampMillis }
                    trySend(messages)
                }
            }

        awaitClose { listener.remove() }
    }

    // --- School Logs & Activity Purge / Clearance ---

    suspend fun clearSchoolChatMessages(schoolId: String = DEFAULT_SCHOOL_ID): Boolean {
        val schoolDoc = getSchoolDoc(schoolId) ?: return false
        return try {
            val snapshot = schoolDoc.collection(COLLECTION_CHAT_MESSAGES).get().await()
            val batch = firestore?.batch()
            if (batch != null) {
                for (doc in snapshot.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit().await()
            }
            Log.d(TAG, "Successfully cleared all chat messages for school $schoolId from Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing school chat messages from Firestore: ${e.message}", e)
            false
        }
    }

    suspend fun clearSchoolAttendance(schoolId: String = DEFAULT_SCHOOL_ID): Boolean {
        val schoolDoc = getSchoolDoc(schoolId) ?: return false
        return try {
            val snapshot = schoolDoc.collection(COLLECTION_ATTENDANCE).get().await()
            val batch = firestore?.batch()
            if (batch != null) {
                for (doc in snapshot.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit().await()
            }
            Log.d(TAG, "Successfully cleared attendance logs for school $schoolId from Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing school attendance from Firestore: ${e.message}", e)
            false
        }
    }

    suspend fun clearAllSchoolActivityLogs(schoolId: String = DEFAULT_SCHOOL_ID): Boolean {
        return try {
            clearSchoolChatMessages(schoolId)
            clearSchoolAttendance(schoolId)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing all school activity logs: ${e.message}", e)
            false
        }
    }

    // --- Multi-Tenant Cross-Device Pull & Verification Helpers ---

    suspend fun getSchoolProfileOnce(schoolId: String): SchoolProfile? {
        val schoolDoc = getSchoolDoc(schoolId) ?: return null
        return try {
            val doc = schoolDoc.collection(COLLECTION_PROFILES).document("main_profile").get().await()
            if (doc.exists()) {
                SchoolProfile(
                    id = doc.getLong("id") ?: 1L,
                    schoolCode = doc.getString("schoolCode") ?: schoolId,
                    schoolName = doc.getString("schoolName") ?: "School",
                    schoolMotto = doc.getString("schoolMotto") ?: "",
                    schoolAddress = doc.getString("schoolAddress") ?: "",
                    schoolEmail = doc.getString("schoolEmail") ?: "",
                    schoolPhone = doc.getString("schoolPhone") ?: "",
                    academicSession = doc.getString("academicSession") ?: "2025/2026",
                    currentTerm = doc.getString("currentTerm") ?: "1st Term",
                    principalName = doc.getString("principalName") ?: "School Principal",
                    schoolLogoBadge = doc.getString("schoolLogoBadge") ?: "SCH"
                )
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching school profile: ${e.message}", e)
            null
        }
    }

    suspend fun fetchAllUsersForSchool(schoolId: String): List<SchoolUser> {
        val schoolDoc = getSchoolDoc(schoolId) ?: return emptyList()
        return try {
            val snapshot = schoolDoc.collection(COLLECTION_USERS).get().await()
            snapshot.documents.mapNotNull { doc ->
                try {
                    val id = doc.getString("id") ?: doc.id
                    val name = doc.getString("name") ?: ""
                    val roleStr = doc.getString("role") ?: SchoolRole.STUDENT.name
                    val role = try { SchoolRole.valueOf(roleStr) } catch (e: Exception) { SchoolRole.STUDENT }
                    val email = doc.getString("email") ?: ""
                    val phone = doc.getString("phone") ?: ""
                    val passcode = doc.getString("passcode") ?: "1234"
                    val className = doc.getString("className") ?: ""
                    val assignedSubjects = doc.getString("assignedSubjects") ?: ""
                    val studentChildId = doc.getString("studentChildId")?.takeIf { it.isNotBlank() }
                    val studentChildName = doc.getString("studentChildName")?.takeIf { it.isNotBlank() }
                    val avatarColorHex = doc.getString("avatarColorHex") ?: "#1E40AF"
                    val photoUri = doc.getString("photoUri")?.takeIf { it.isNotBlank() }
                    val gender = doc.getString("gender") ?: "Female"
                    val dateOfBirth = doc.getString("dateOfBirth") ?: "2009-05-14"
                    val guardianName = doc.getString("guardianName") ?: ""
                    val guardianPhone = doc.getString("guardianPhone") ?: ""
                    val guardianEmail = doc.getString("guardianEmail") ?: ""
                    val residentialAddress = doc.getString("residentialAddress") ?: ""
                    val bloodGroup = doc.getString("bloodGroup") ?: "O+"
                    val genotype = doc.getString("genotype") ?: "AA"
                    val admissionDate = doc.getString("admissionDate") ?: "2024-09-10"
                    val stateOfOrigin = doc.getString("stateOfOrigin") ?: "Lagos"

                    SchoolUser(
                        id = id,
                        name = name,
                        role = role,
                        email = email,
                        phone = phone,
                        passcode = passcode,
                        className = className,
                        assignedSubjects = assignedSubjects,
                        studentChildId = studentChildId,
                        studentChildName = studentChildName,
                        avatarColorHex = avatarColorHex,
                        photoUri = photoUri,
                        gender = gender,
                        dateOfBirth = dateOfBirth,
                        guardianName = guardianName,
                        guardianPhone = guardianPhone,
                        guardianEmail = guardianEmail,
                        residentialAddress = residentialAddress,
                        bloodGroup = bloodGroup,
                        genotype = genotype,
                        admissionDate = admissionDate,
                        stateOfOrigin = stateOfOrigin
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching school users: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun fetchAllClassesForSchool(schoolId: String): List<SchoolClass> {
        val schoolDoc = getSchoolDoc(schoolId) ?: return emptyList()
        return try {
            val snapshot = schoolDoc.collection(COLLECTION_CLASSES).get().await()
            snapshot.documents.mapNotNull { doc ->
                try {
                    SchoolClass(
                        id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
                        name = doc.getString("name") ?: "",
                        level = doc.getString("level") ?: "",
                        arm = doc.getString("arm") ?: "",
                        classTeacherId = doc.getString("classTeacherId") ?: "",
                        classTeacherName = doc.getString("classTeacherName") ?: "",
                        studentCount = doc.getLong("studentCount")?.toInt() ?: 30,
                        room = doc.getString("room") ?: "Main Block"
                    )
                } catch (e: Exception) { null }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchAllSubjectsForSchool(schoolId: String): List<SchoolSubject> {
        val schoolDoc = getSchoolDoc(schoolId) ?: return emptyList()
        return try {
            val snapshot = schoolDoc.collection(COLLECTION_SUBJECTS).get().await()
            snapshot.documents.mapNotNull { doc ->
                try {
                    SchoolSubject(
                        id = doc.getLong("id") ?: 0L,
                        name = doc.getString("name") ?: "",
                        code = doc.getString("code") ?: "",
                        classLevel = doc.getString("classLevel") ?: "",
                        teacherId = doc.getString("teacherId") ?: "",
                        teacherName = doc.getString("teacherName") ?: "",
                        colorHex = doc.getString("colorHex") ?: "#1E40AF",
                        periodsPerWeek = doc.getLong("periodsPerWeek")?.toInt() ?: 4
                    )
                } catch (e: Exception) { null }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchAllExamsForSchool(schoolId: String): List<CbtExam> {
        val schoolDoc = getSchoolDoc(schoolId) ?: return emptyList()
        return try {
            val snapshot = schoolDoc.collection(COLLECTION_CBT_EXAMS).get().await()
            snapshot.documents.mapNotNull { doc ->
                try {
                    CbtExam(
                        id = doc.getLong("id") ?: 0L,
                        title = doc.getString("title") ?: "",
                        subjectName = doc.getString("subjectName") ?: "",
                        className = doc.getString("className") ?: "All",
                        teacherId = doc.getString("teacherId") ?: "",
                        teacherName = doc.getString("teacherName") ?: "",
                        examType = doc.getString("examType") ?: "TEST",
                        durationMinutes = doc.getLong("durationMinutes")?.toInt() ?: 30,
                        totalMarks = doc.getLong("totalMarks")?.toInt() ?: 20,
                        passMark = doc.getLong("passMark")?.toInt() ?: 10,
                        isPublished = doc.getBoolean("isPublished") ?: true,
                        instructions = doc.getString("instructions") ?: "",
                        createdDateMillis = doc.getLong("createdDateMillis") ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) { null }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchAllAnnouncementsForSchool(schoolId: String): List<SchoolAnnouncement> {
        val schoolDoc = getSchoolDoc(schoolId) ?: return emptyList()
        return try {
            val snapshot = schoolDoc.collection(COLLECTION_ANNOUNCEMENTS).get().await()
            snapshot.documents.mapNotNull { doc ->
                try {
                    SchoolAnnouncement(
                        id = doc.getLong("id") ?: 0L,
                        title = doc.getString("title") ?: "",
                        content = doc.getString("content") ?: "",
                        targetAudience = doc.getString("targetAudience") ?: "ALL",
                        senderName = doc.getString("senderName") ?: "Admin",
                        senderRole = doc.getString("senderRole") ?: "ADMIN",
                        category = doc.getString("category") ?: "GENERAL",
                        isUrgent = doc.getBoolean("isUrgent") ?: false,
                        postedAtMillis = doc.getLong("postedAtMillis") ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) { null }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
