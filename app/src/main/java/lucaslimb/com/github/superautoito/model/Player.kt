package lucaslimb.com.github.superautoito.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Player(
    val id: String,
    val name: String,
    val hand: List<Character> = emptyList(),
    val isHost: Boolean = false
) : Parcelable
