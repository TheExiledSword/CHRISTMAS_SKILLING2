package net.botwithus

import net.botwithus.internal.scripts.ScriptDefinition
import net.botwithus.rs3.game.Area
import net.botwithus.rs3.game.Client
import net.botwithus.rs3.game.Client.getLocalPlayer
import net.botwithus.rs3.game.Coordinate
import net.botwithus.rs3.game.movement.Movement
import net.botwithus.rs3.game.queries.builders.animations.SpotAnimationQuery
import net.botwithus.rs3.game.queries.builders.components.ComponentQuery
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery
import net.botwithus.rs3.game.scene.entities.animation.SpotAnimation
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer
import net.botwithus.rs3.game.scene.entities.`object`.SceneObject
import net.botwithus.rs3.script.Execution
import net.botwithus.rs3.script.LoopingScript
import net.botwithus.rs3.script.config.ScriptConfig
import net.botwithus.util.Log
import java.util.*


class KotlinSkeleton(
    name: String,
    scriptConfig: ScriptConfig,
    scriptDefinition: ScriptDefinition
) : LoopingScript (name, scriptConfig, scriptDefinition) {

    private val random: Random = Random()
    var botState: BotState = BotState.CHECKING_POS

    private var sceneObjectCauldron: SceneObject? = null
    private val log = Log(true)

    enum class BotState {
        IDLE,
        AFK_SIMULATION,
        CHECKING_POS,
        DEFAULT_COOKING,
        MOVING_TO_COOKING,
    }

    /**
     * Not implemented yet
     */
    enum class BotMode {
        SUICIDE_BOT,
        SEMI_AFK,
        FULL_AFK
    }

    override fun initialize(): Boolean {
        super.initialize()
        this.sgc = KotlinSkeletonGraphicsContext(this, console)
        return true
    }

    override fun onLoop() {
        val player = getLocalPlayer()
        if (Client.getGameState() != Client.GameState.LOGGED_IN || player == null || botState == BotState.IDLE) {
            Execution.delay(random.nextLong(2500,5500))
            return
        }

        when (botState) {
            BotState.MOVING_TO_COOKING -> {
                Execution.delay(handleMovingToCooking(player))
                return
            }

            BotState.DEFAULT_COOKING -> {
                Execution.delay(handleDefaultCooking(player))
                return
            }

            BotState.IDLE -> {
                log.error("We're idle! not implemented")
                Execution.delay(random.nextLong(1500,5000))
                return
            }

            BotState.AFK_SIMULATION -> {
                log.error("We're afk! not implemented")
                Execution.delay(random.nextLong(1500,5000))
                return
            }

            BotState.CHECKING_POS -> {
                handlePositionCheck(player)
                Execution.delay(random.nextLong(1500,5000))
                return
            }
        }

    }

    private fun getRandomDelay(): Long {
        val minDelay = 1000L
        val maxDelay = 15000L
        return random.nextLong(minDelay, maxDelay + 1)
    }

    private fun handleDefaultCooking(player: LocalPlayer): Long {
        spotAnim = SpotAnimationQuery.newQuery().ids(7164).results().firstOrNull()
        spotAnimArea = spotAnim?.area
        sceneObject = SceneObjectQuery.newQuery().inside(spotAnimArea).results().firstOrNull()
        if (sceneObject != null) {
            log.success("Interacted with ${sceneObject!!.name} : ${sceneObject!!.interact("Take")}")
            takeSpotAnim = true
            return getRandomDelay()
        } else {
            if (player.animationId != 36391) {
                log.success("Interacted with ${sceneObjectCauldron!!.name} : ${sceneObjectCauldron!!.interact("Assist Aoife")}")
                return getRandomDelay()
            }
        }
        if (takeSpotAnim) {
            if (sceneObjectCauldron != null) {
                log.success("Interacted with ${sceneObjectCauldron!!.name} : ${sceneObjectCauldron!!.interact("Assist Aoife")}")
                takeSpotAnim = false
                return getRandomDelay()
            }
        }
        log.warn("skipping everything as there is nothing to do")
        return getRandomDelay()
    }

    private fun handlePositionCheck(player: LocalPlayer) {
        log.debug("Checking position")
        sceneObjectCauldron = SceneObjectQuery.newQuery().ids(131826).results().firstOrNull()
        if (sceneObjectCauldron == null) {
            log.debug("Not inside christmas event")
            val button1 = ComponentQuery.newQuery(1431).subComponentIndex(4).spriteId(25722).results().firstOrNull()
            if (button1 != null) {
                log.success("Interacted with community button: ${button1.interact("Open")}")
                Execution.delay(random.nextLong(1500, 5000))
                val teleportButton = ComponentQuery.newQuery(1319).componentIndex(25).results().firstOrNull()
                if (teleportButton != null) {
                    log.success("Interacted with teleportButton: ${teleportButton.interact("Select")}")
                    Execution.delay(random.nextLong(5000, 8000))
                    if (player.distanceTo(Coordinate(5216, 9787, 0)) < 20) {
                        log.success("inside christmas event area")
                        botState = BotState.MOVING_TO_COOKING
                        sceneObjectCauldron = SceneObjectQuery.newQuery().ids(131826).results().firstOrNull()
                        Execution.delay(random.nextLong(1500, 5000))
                        return
                    } else {
                        log.error("Failed to teleport to christmas event distance is ${player.distanceTo(Coordinate(5216, 9787, 0))}")
                    }
                    return
                } else {
                    log.error("Failed to find teleportButton")
                }
            } else {
                log.error("Failed to find community button")
            }
        } else {
            log.success("Inside christmas event")
            botState = BotState.MOVING_TO_COOKING
        }
    }

    private fun handleMovingToCooking(player: LocalPlayer): Long {
        log.debug("defaulting to cooking")
        if (sceneObjectCauldron?.coordinate?.let { player.distanceTo(it) }!! > 10) {
            log.debug("Walking to cauldron")
            val leftCorner =
                Coordinate(5248, 9772, 0)
            val rightCorner =
                Coordinate(5242,9768,0)
            val area = Area.Rectangular(leftCorner, rightCorner)
            val randomCoordinate = area.randomWalkableCoordinate
            Movement.walkTo(randomCoordinate.x, randomCoordinate.y, false)
            Execution.delayUntil(5000) { player.distanceTo(sceneObjectCauldron?.coordinate!!) < 10 }
            if (player.distanceTo(sceneObjectCauldron?.coordinate!!) < 10) {
                log.success("Arrived at cauldron")
                botState = BotState.DEFAULT_COOKING
                return random.nextLong(1500, 3000)
            } else {
                log.error("Failed to arrive at cauldron")
                botState = BotState.CHECKING_POS
                return random.nextLong(1500, 3000)
            }
        } else {
            log.success("Already at cauldron")
            botState = BotState.DEFAULT_COOKING
            return random.nextLong(1500, 3000)
        }
    }
        

    private var takeSpotAnim = false
    private var spotAnim: SpotAnimation? = null
    private var spotAnimArea: Area? = null
    private var sceneObject: SceneObject? = null

    private fun oldhandleskilling(player: LocalPlayer): Long {

        spotAnim = SpotAnimationQuery.newQuery().ids(7164).results().firstOrNull()
        spotAnimArea = spotAnim?.area
        sceneObject = SceneObjectQuery.newQuery().inside(spotAnimArea).results().firstOrNull()
        val sceneObjectCauldron = SceneObjectQuery.newQuery().ids(131826).results().firstOrNull()
        if (sceneObjectCauldron != null) {
            println("found cauldron: ${sceneObjectCauldron.name}")
        }

        random.nextLong(1500, 3000)

        if (sceneObject != null) {
            println("Interacted with ${sceneObject!!.name} : ${sceneObject!!.interact("Take")}")
            takeSpotAnim = true
            return random.nextLong(1500, 3000)
        }

        if (takeSpotAnim) {
            if (sceneObjectCauldron != null) {
                println("Interacted with ${sceneObjectCauldron.name} : ${sceneObjectCauldron.interact("Assist Aoife")}")
                takeSpotAnim = false
                return random.nextLong(1500, 3000)
            }
        }

        println("skipping everything as there is nothing to do")
        return random.nextLong(1500, 3000)
    }

}