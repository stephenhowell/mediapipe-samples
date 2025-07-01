package com.google.mediapipe.examples.poselandmarker

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import org.json.JSONObject
import org.json.JSONArray
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class PoseWebSocketClient(serverUri: URI) : WebSocketClient(serverUri) {

    override fun onOpen(handshakedata: ServerHandshake) {
        println("WebSocket Connected")
    }

    override fun onMessage(message: String) {
        // We only send, don't receive
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        println("WebSocket Closed: $reason")
    }

    override fun onError(ex: Exception) {
        println("WebSocket Error: ${ex.message}")
    }

    private fun transformToScratch(x: Float, y: Float): Pair<Float, Float> {
        val scratchX = (x * 480) - 240
        val scratchY = 180 - (y * 360)
        return Pair(scratchX, scratchY)
    }

    private fun landmarkToScratchCoord(landmark: com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmark): JSONArray {
        val (x, y) = transformToScratch(landmark.x(), landmark.y())
        return JSONArray().apply {
            put(x)
            put(y)
            put(0.0) // z coordinate - deprecated
        }
    }

    fun sendPoseData(result: PoseLandmarkerResult) {
        if (!isOpen) return

        try {
            val landmarks = result.landmarks().firstOrNull() ?: return
            val joints = JSONObject()

            // Transform and map MediaPipe landmarks to Kinect joints
            // We'll fill this in next...

            val message = JSONObject().apply {
                put("type", "body")
                put("bodyIndex", 0)
                put("joints", joints)
                put("rightHandState", "Unknown")
                put("leftHandState", "Unknown")
            }

            send(message.toString())
        } catch (e: Exception) {
            println("Error sending pose: ${e.message}")
        }
    }
}