package com.example.mobilelearningapp.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.databinding.ActivityVideoPlayerBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView

class VideoPlayerActivity : BaseActivity() {

    private var binding : ActivityVideoPlayerBinding? = null
    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        playerView = findViewById(R.id.player_view)

        val videoUrl = intent.getStringExtra("VIDEO_URL") ?: return

        initializePlayer(videoUrl)
    }

    private fun initializePlayer(videoUrl: String) {
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }
}
