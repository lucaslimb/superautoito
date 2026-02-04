package lucaslimb.com.github.superautoito.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameState(
    val currentRound: Int = 1,
    val totalRounds: Int = 5,
    val playerWins: Int = 0,
    val playerLosses: Int = 0,
    val playerTeam: List<Character>,
    val playerReserve: List<Character>,
    val opponentTeam: List<Character>,
    val opponentReserve: List<Character>,
    val playerCanBuyCard: Boolean = false,
    val lastRoundWinner: Int = 0 // 1=player, -1=opponent, 0=empate
) : Parcelable {

    fun isGameOver(): Boolean = currentRound > totalRounds

    fun getFinalWinner(): Int {
        return when {
            playerWins > playerLosses -> 1
            playerLosses > playerWins -> -1
            else -> 0
        }
    }
}