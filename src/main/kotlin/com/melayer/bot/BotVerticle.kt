package com.melayer.bot

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.bridge.BridgeOptions
import io.vertx.ext.bridge.PermittedOptions
import io.vertx.ext.eventbus.bridge.tcp.TcpEventBusBridge
import org.alicebot.ab.*
/**
 * Created by aniruddha on 21/7/17.
 */
class BotVerticle : AbstractVerticle() {

    private val log : Logger = LoggerFactory.getLogger(BotVerticle::class.java)

    @Throws(Exception::class)
    override fun start(future: Future<Void>) {
        val bridge = TcpEventBusBridge.create(
                vertx,
                BridgeOptions()
                        .addInboundPermitted(PermittedOptions().setAddressRegex("bot\\.[a-zA-Z0-9]+"))
                        .addOutboundPermitted(PermittedOptions().setAddressRegex("panda\\.[a-zA-Z0-9]+"))
        )
        bridge.listen(8090) { log.info("Panda listening for TCP connections on 8090 :)") }

        val chat  = initBot()

        vertx.eventBus().consumer<JsonObject>("bot.DHL123", {
            val botSays = chat.multisentenceRespond(it.body().getString("pandaSays"))
            vertx.eventBus().send(
                    "panda.gaga123",JsonObject().put("botSays",botSays))
        })
    }

    fun initBot() : Chat {

        MagicStrings.root_path = "/home/ubuntu/aiml-bots"
        AIMLProcessor.extension = PCAIMLProcessorExtension()

        val botName = "alice2"
        val action = "chat"
        log.info(MagicStrings.programNameVersion)

        log.info("trace mode = " + MagicBooleans.trace_mode)
        Graphmaster.enableShortCuts = true
        val bot = Bot(botName, MagicStrings.root_path, action) //

        log.info(bot.brain.upgradeCnt.toString() + " brain upgrades")
        bot.brain.nodeStats()
        val chatSession = Chat(bot)

        return chatSession
    }
}