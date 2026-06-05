package com.example.data.repository

import com.example.data.dao.FitTrackDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class FitTrackRepository(private val dao: FitTrackDao) {

    val allPrograms: Flow<List<WorkoutProgram>> = dao.getAllPrograms()
    val allExercises: Flow<List<Exercise>> = dao.getAllExercises()
    val allWorkoutLogs: Flow<List<WorkoutSessionLog>> = dao.getAllWorkoutLogs()
    val allWeightLogs: Flow<List<WeightLog>> = dao.getAllWeightLogs()
    val allPhotos: Flow<List<ProgressPhoto>> = dao.getAllPhotos()
    val allRecords: Flow<List<PersonalRecord>> = dao.getAllRecords()
    val userProfile: Flow<UserProfile?> = dao.getUserProfile()

    suspend fun insertProgram(program: WorkoutProgram): Long = dao.insertProgram(program)
    suspend fun deleteProgramById(id: Int) = dao.deleteProgramById(id)

    suspend fun insertWorkoutLog(log: WorkoutSessionLog): Long = dao.insertWorkoutLog(log)

    suspend fun insertWeightLog(log: WeightLog): Long = dao.insertWeightLog(log)
    suspend fun deleteWeightLogById(id: Int) = dao.deleteWeightLogById(id)

    suspend fun insertPhoto(photo: ProgressPhoto): Long = dao.insertPhoto(photo)
    suspend fun deletePhotoById(id: Int) = dao.deletePhotoById(id)

    suspend fun insertRecord(record: PersonalRecord): Long = dao.insertRecord(record)
    suspend fun getBestRecordByExercise(name: String): PersonalRecord? = dao.getBestRecordByExercise(name)

    suspend fun saveProfile(profile: UserProfile): Long = dao.insertOrUpdateProfile(profile)

    suspend fun seedExercisesIfEmpty() {
        // Run seed check
        val currentExercises = dao.getAllExercises().first()
        if (currentExercises.isEmpty()) {
            val seededList = listOf(
                // Göğüs
                Exercise(
                    id = "bench_press",
                    name = "Bench Press",
                    muscleGroup = "Göğüs",
                    description = "Geleneksel göğüs geliştirme ve üst vücut güç egzersizi.",
                    difficulty = "Orta",
                    howTo = "Düz sehpaya sırt üstü uzanın. Barları omuz genişliğinden biraz daha geniş bir açıyla kavrayın. Barı yavaşça göğsünüze indirin ve ardından kollarınızı kilitlemeden yukarıya doğru itin.",
                    tips = "Ayaklarınızı yerde sabit ve sağlam tutun. Göğsünüzü yukarıda tutarak omuz bıçaklarınızı sıkıştırın.",
                    commonMistakes = "Barı göğsünüzden hızlıca sektirmek, beli gereğinden fazla yüksekliğe bükmek."
                ),
                Exercise(
                    id = "incline_db_press",
                    name = "Incline Dumbbell Press",
                    muscleGroup = "Göğüs",
                    description = "Üst göğüs liflerini hedefleyen dambıl egzersizi.",
                    difficulty = "Orta",
                    howTo = "30-45 derecelik eğimli sehpaya oturun. Dambılları göğüs seviyenizde tutun. Nefes verirken presleyin, yukarıda birleştirin ve yavaşça kilitlemeden indirin.",
                    tips = "Dambılların birbirine çarpmasından kaçının, hareketi kontrollü yapın.",
                    commonMistakes = "Açının 45 dereceden fazla olması durumunda yükün omuzlara binmesi."
                ),
                // Sırt
                Exercise(
                    id = "deadlift",
                    name = "Deadlift",
                    muscleGroup = "Sırt",
                    description = "Tüm arka zincir kaslarını aktif eden temel güç egzersizi.",
                    difficulty = "Zor",
                    howTo = "Barın önünde dik durun. Kalçanızı geriye bükerek barı kavrayın. Sırtınızı düz tutarak, kalça ve dizler yardımıyla barı yukarı kaldırın.",
                    tips = "Başınızı nötr tutun, sırtınızda bükülme olmamasına ekstra özen gösterin.",
                    commonMistakes = "Sırtı yuvarlamak, yükü bacaklar yerine sadece belden çekmeye çalışmak."
                ),
                Exercise(
                    id = "lat_pulldown",
                    name = "Lat Pulldown",
                    muscleGroup = "Sırt",
                    description = "Kanat kaslarını (Latissimus dorsi) hedefleyen çekiş egzersizi.",
                    difficulty = "Kolay",
                    howTo = "Makineye oturun, barı geniş açıyla kavrayın. Göğsünüzü yukarıda tutarak barı köprücük kemiğinize doğru çekin, yavaşça geriye bırakın.",
                    tips = "Hareketi bicepslerinizle değil, sırt kaslarınızı sıkarak yapmaya çalışın.",
                    commonMistakes = "Gövdeyi çok fazla arkaya sallamak, barı çok hızlı bırakmak."
                ),
                // Omuz
                Exercise(
                    id = "overhead_press",
                    name = "Overhead Press",
                    muscleGroup = "Omuz",
                    description = "Omuz başlarını ve üst vücudu güçlendiren ayakta pres hareketi.",
                    difficulty = "Zor",
                    howTo = "Barı omuz hizasında kavrayın. Karın ve kalçanızı sıkarak barı başınızın üzerine doğru dik bir şekilde itin.",
                    tips = "Bileklerinizin arkaya bükülmesini önleyin, karın bölgenizi sürekli aktif tutun.",
                    commonMistakes = "Dizlerden destek alarak push press yapmak (eğer saf OHP hedefleniyorsa), beli arkaya çok fazla kaydırmak."
                ),
                Exercise(
                    id = "lateral_raise",
                    name = "Lateral Raise",
                    muscleGroup = "Omuz",
                    description = "Yan omuzları izole ederek omuz genişliğini artıran dambıl hareketi.",
                    difficulty = "Kolay",
                    howTo = "Dambılları yanlarınızda tutun. Dirseklerinizi hafif bükük tutarak dambılları omuz hizasına gelinceye dek yanlara doğru kaldırın.",
                    tips = "Kaldırırken serçe parmağınızı hafif yukarı döndürmek omuz izolasyonunu artırır.",
                    commonMistakes = "Ağır dambıllarla gövdeyi sallayarak momentum kullanmak."
                ),
                // Kol
                Exercise(
                    id = "bicep_curl",
                    name = "Barbell Bicep Curl",
                    muscleGroup = "Kol",
                    description = "Ön kol (biceps) kaslarını izole eden klasik curl hareketi.",
                    difficulty = "Kolay",
                    howTo = "Ayakta dik durun, barı avuç içleri karşıya bakacak şekilde kavrayın. Dirsekleri gövdeye sabitleyerek barı omuz seviyenize bükün.",
                    tips = "Dirseklerinizi öne veya arkaya kaydırmaktan kaçının.",
                    commonMistakes = "Vücudu sallayarak belden destek almak."
                ),
                Exercise(
                    id = "tricep_pushdown",
                    name = "Cable Tricep Pushdown",
                    muscleGroup = "Kol",
                    description = "Arka kol (triceps) kaslarını kablo yardımıyla izole eden egzersiz.",
                    difficulty = "Kolay",
                    howTo = "Kablo makinesinin karşısında durun. Barı kavrayıp dirseklerinizi yanlarınıza sabitleyin. Dirsekleri kilitlemeye yakın açıyla aşağı doğru itin.",
                    tips = "Sadece ön kolları hareket ettirin, omuzları sabit tutun.",
                    commonMistakes = "Dirsekleri yan gövdeden açmak, aşırı öne eğilmek."
                ),
                // Bacak
                Exercise(
                    id = "squat",
                    name = "Squat",
                    muscleGroup = "Bacak",
                    description = "Alt vücudun (quads, hamstrings, glutes) kralı kabul edilen temel egzersiz.",
                    difficulty = "Zor",
                    howTo = "Barı trapez kaslarınıza yerleştirin. Ayaklar omuz genişliğinde açık dursun. Sandalyeye oturur gibi kalçanızı geriye vererek dizlerinizi bükün ve derinleşin.",
                    tips = "Dizlerinizin ayak parmak uçlarınızı geçmemesine ve içe bükülmemesine dikkat edin.",
                    commonMistakes = "Yeterince derinleşmemek (yarım squat), topukların yerden kalkması."
                ),
                Exercise(
                    id = "leg_extension",
                    name = "Leg Extension",
                    muscleGroup = "Bacak",
                    description = "Ön bacak kaslarını izole eden makine egzersizi.",
                    difficulty = "Kolay",
                    howTo = "Makineye oturun, ayak bileklerinizi ped altına yerleştirin. Bacaklarınızı dümdüz olana dek yukarı kaldırın, yavaşça indirin.",
                    tips = "Yukarıda 1 saniye kası sıkarak kasılmayı artırın.",
                    commonMistakes = "Koltuk ayarını hatalı yaparak diz eklemine aşırı baskı bindirmek."
                ),
                // Karın
                Exercise(
                    id = "crunch",
                    name = "Karın Crunch",
                    muscleGroup = "Karın",
                    description = "Üst karın kaslarını izole eden etkili sıkıştırma hareketi.",
                    difficulty = "Kolay",
                    howTo = "Yere sırt üstü uzanın, dizleri bükün. Ellerinizi başınızın arkasına hafifçe koyun, omuzları hafifçe yerden kaldırarak karın kaslarınızı sıkıştırın.",
                    tips = "Çenenizi göğsünüze yapıştırmayın, yukarı doğru bakın.",
                    commonMistakes = "Boynu ellerle çekerek zorlamak."
                ),
                Exercise(
                    id = "plank",
                    name = "Plank",
                    muscleGroup = "Karın",
                    description = "Tüm core (merkez) bölgesini çalıştıran statik direnç egzersizi.",
                    difficulty = "Orta",
                    howTo = "Ön kollarınız üzerinde yere uzanın. Vücudunuzu başınızdan topuklarınıza kadar tek bir çizgi halinde düz ve gergin tutun.",
                    tips = "Karın ve kalçayı sürekli sıkın, nefes almayı unutmayın.",
                    commonMistakes = "Kalçayı çok yukarı kaldırmak ya da aşağıya düşürerek beli zorlamak."
                ),
                // Kardiyo
                Exercise(
                    id = "treadmill_run",
                    name = "Koşu Bandı",
                    muscleGroup = "Kardiyo",
                    description = "Kalp ve damar sağlığını destekleyen kardiyovasküler koşu/yürüyüş aktivitesi.",
                    difficulty = "Orta",
                    howTo = "Koşu bandına çıkın, seviyenizi belirleyin. Tempo ve eğime göre yürüyün ya da koşun.",
                    tips = "Postürünüzü dik tutun ve adımlarınızı topuktan parmak ucuna doğru basın.",
                    commonMistakes = "Tutacaklara asılarak ağırlığı makineye vermek."
                )
            )
            dao.insertExercises(seededList)

            // Seed a default workout program as well to improve initial UX!
            val defaultProgram = WorkoutProgram(
                name = "Full Body Güç ve Kas",
                goal = "Kas kazanımı",
                daysPerWeek = 3,
                exercisesString = "squat,bench_press,deadlift,lat_pulldown,overhead_press"
            )
            dao.insertProgram(defaultProgram)

            // Seed basic default records to start with
            dao.insertRecord(PersonalRecord(exerciseName = "Bench Press", weight = 60.0))
            dao.insertRecord(PersonalRecord(exerciseName = "Squat", weight = 80.0))
            dao.insertRecord(PersonalRecord(exerciseName = "Deadlift", weight = 100.0))
            dao.insertRecord(PersonalRecord(exerciseName = "Overhead Press", weight = 40.0))

            // Seed user profile
            dao.insertOrUpdateProfile(UserProfile(
                id = 1,
                name = "Yusuf Demir",
                height = 180.0,
                weight = 82.5,
                age = 26,
                goal = "Kas kazanımı",
                isKg = true,
                isDarkTheme = true,
                notificationsEnabled = true,
                isOnboarded = false
            ))

            // Seed some sample weight records to draw a cool graph on start!
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L
            dao.insertWeightLog(WeightLog(weight = 84.1, timestamp = now - 15 * dayMs, notes = "Yarı ödemli başlangıç"))
            dao.insertWeightLog(WeightLog(weight = 83.5, timestamp = now - 11 * dayMs, notes = "Su atımı başarılı"))
            dao.insertWeightLog(WeightLog(weight = 83.0, timestamp = now - 7 * dayMs, notes = "Fena değil, güç kaybı yok"))
            dao.insertWeightLog(WeightLog(weight = 82.5, timestamp = now, notes = "Hedefe adım adım"))
        }
    }
}
