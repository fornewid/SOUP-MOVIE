package soup.movie.data.helper

import android.content.Intent
import android.os.Bundle
import androidx.annotation.ColorRes
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.ChronoUnit
import soup.movie.R
import soup.movie.data.model.Movie
import soup.movie.util.fromJson
import soup.movie.util.toJson

private const val KEY_JSON = "json"

fun Bundle.restoreFrom(): Movie? = getString(KEY_JSON)?.fromJson()

fun Intent.restoreFrom(): Movie? = getStringExtra(KEY_JSON).fromJson()

fun Movie.saveTo(bundle: Bundle) = bundle.putString(KEY_JSON, toJson())

fun Movie.saveTo(intent: Intent): Intent = intent.putExtra(KEY_JSON, toJson())

fun Movie.toShareDescription(): String = "제목: $title\n개봉일: $openDate\n연령제한: $age"

fun Movie.toDescription(): String = "$openDate / $age / 선호도: $egg"

@ColorRes
fun Movie.getColorAsAge(): Int = when (age) {
    "전체 관람가" -> R.color.green
    "12세 관람가" -> R.color.blue
    "15세 관람가" -> R.color.amber
    "청소년관람불가" -> R.color.red
    else -> R.color.grey
}

fun Movie.getSimpleAgeLabel(): String = when (age) {
    "전체 관람가" -> "전체"
    "12세 관람가" -> "12"
    "15세 관람가" -> "15"
    "청소년관람불가" -> "청불"
    else -> "미정"
}

fun Movie.getDDayLabel(): String? = openDate()?.let {
    val dDay = ChronoUnit.DAYS.between(today(), it)
    return when {
        dDay <= 0 -> "NOW"
        else -> "D-$dDay"
    }
}

fun Movie.hasOpenDate(): Boolean = openDate()?.let { true } ?: false

fun Movie.isPlan(): Boolean = isNow.not() and hasOpenDate()

fun Movie.eggIsOver95(): Boolean = (egg != "?") and (egg >= "95")

fun Movie.hasUnknownEgg(): Boolean = egg == "?"

fun Movie.isHot(): Boolean = eggIsOver95()

fun Movie.isNew(): Boolean = isNow and isInTheThreeDays() or (isInThePastWeek() and hasUnknownEgg())

fun Movie.isInThePastWeek(): Boolean = isIn(-7..0)

fun Movie.isInTheThreeDays(): Boolean = isIn(-2..0)

fun Movie.isInTheNextWeek(): Boolean = isIn(0..7)

fun Movie.isIn(dayRange: IntRange): Boolean = openDate()?.let {
    ChronoUnit.DAYS.between(today(), it) in dayRange
} ?: false

private fun today(): LocalDate = LocalDate.now(ZoneId.of("Asia/Seoul"))

private fun Movie.openDate(): LocalDate? = openDate.split(".").let {
    return if (it.size == 3) {
        LocalDate.of(it[0].toInt(), it[1].toInt(), it[2].toInt())
    } else {
        null
    }
}
