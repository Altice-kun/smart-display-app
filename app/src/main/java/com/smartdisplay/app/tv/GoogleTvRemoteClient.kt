package com.smartdisplay.app.tv

import com.smartdisplay.app.tv.proto.RemoteAppLinkLaunchRequest
import com.smartdisplay.app.tv.proto.RemoteConfigure
import com.smartdisplay.app.tv.proto.RemoteDeviceInfo
import com.smartdisplay.app.tv.proto.RemoteDirection
import com.smartdisplay.app.tv.proto.RemoteKeyCode
import com.smartdisplay.app.tv.proto.RemoteKeyInject
import com.smartdisplay.app.tv.proto.RemoteMessage
import com.smartdisplay.app.tv.proto.RemotePingResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Google TV（Android TV Remote protocol v2）へのコマンド送信クライアント。
 *
 * 重要: これはペアリング【後】の通信だけを扱います。
 * 初回ペアリング（自己署名証明書の生成、ポート6466でのPINコード確認）は
 * このファイルには含まれていません。理由: このプロトコルは非公式の解析情報しか
 * 存在せず、ここだけは正確なバイト列を裏取りできていないため、憶測で実装するより
 * 実績のある実装（tronikos/androidtvremote2 や dgmltn/Dpad の :protocol モジュールなど）
 * から移植することを強く推奨します。詳細はREADMEを参照してください。
 *
 * ペアリングが完了し、クライアント証明書(KeyStore)が手元にある前提で、
 * ポート6467への接続・キー送信・アプリ起動（ディープリンク）を行います。
 */
class GoogleTvRemoteClient(
    private val host: String,
    private val port: Int = 6467,
    private val clientKeyStore: KeyStore,
    private val keyStorePassword: CharArray
) {
    private var socket: SSLSocket? = null
    private var output: OutputStream? = null
    private var input: InputStream? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    /** TVに接続し、ハンドシェイク用のConfigureメッセージを送る。 */
    fun connect(deviceModel: String = "SmartDisplay", appVersion: String = "1.0") {
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply {
            init(clientKeyStore, keyStorePassword)
        }
        // TV側は自己署名証明書を使うため、ここでは証明書検証をスキップしている。
        // 本番実装では、初回ペアリング時に受け取ったTVの証明書をピン留めして検証すること。
        val trustAll = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
        val sslContext = SSLContext.getInstance("TLSv1.2").apply {
            init(kmf.keyManagers, arrayOf<TrustManager>(trustAll), null)
        }
        val s = (sslContext.socketFactory.createSocket(host, port) as SSLSocket).apply {
            startHandshake()
        }
        socket = s
        output = s.outputStream
        input = s.inputStream

        sendMessage(
            RemoteMessage.newBuilder()
                .setRemoteConfigure(
                    RemoteConfigure.newBuilder()
                        .setCode1(622)
                        .setDeviceInfo(
                            RemoteDeviceInfo.newBuilder()
                                .setModel(deviceModel)
                                .setVendor("SmartDisplay")
                                .setPackageName("com.smartdisplay.app")
                                .setAppVersion(appVersion)
                                .build()
                        ).build()
                ).build()
        )

        scope.launch { listenLoop() }
    }

    /** 例: sendKey(RemoteKeyCode.KEYCODE_DPAD_UP) */
    fun sendKey(keyCode: RemoteKeyCode, direction: RemoteDirection = RemoteDirection.SHORT) {
        sendMessage(
            RemoteMessage.newBuilder()
                .setRemoteKeyInject(
                    RemoteKeyInject.newBuilder().setKeyCode(keyCode).setDirection(direction).build()
                ).build()
        )
    }

    /**
     * アプリ内の特定コンテンツではなく「アプリを起動する」用途では、
     * 各サービスのトップページ相当のディープリンク（App Links）を渡す。
     * 正確なURIはサービスごとに異なるため、実機でTV側のアプリ起動が
     * 意図通りになるか確認してから使うこと。
     */
    fun launchApp(deepLinkUrl: String) {
        sendMessage(
            RemoteMessage.newBuilder()
                .setRemoteAppLinkLaunchRequest(
                    RemoteAppLinkLaunchRequest.newBuilder().setAppLink(deepLinkUrl).build()
                ).build()
        )
    }

    fun disconnect() {
        runCatching { socket?.close() }
        socket = null
        output = null
        input = null
    }

    private fun sendMessage(message: RemoteMessage) {
        val out = output ?: return
        synchronized(out) {
            // protobuf標準のdelimited形式（先頭にvarintでメッセージ長を付与）で送信する。
            message.writeDelimitedTo(out)
            out.flush()
        }
    }

    private fun listenLoop() {
        val stream = input ?: return
        while (socket?.isConnected == true) {
            val msg = runCatching { RemoteMessage.parseDelimitedFrom(stream) }.getOrNull() ?: break
            if (msg.hasRemotePingRequest()) {
                sendMessage(
                    RemoteMessage.newBuilder()
                        .setRemotePingResponse(
                            RemotePingResponse.newBuilder().setVal1(msg.remotePingRequest.val1).build()
                        ).build()
                )
            }
            // 電源状態やアクティブ状態の変化を扱いたい場合は msg.hasRemoteSetActive() 等をここで処理する。
        }
    }
}
