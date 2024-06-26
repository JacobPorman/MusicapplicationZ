package com.example.musicapplication_zingmp3.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.musicapplication_zingmp3.Activity.MainActivity;
import com.example.musicapplication_zingmp3.Adapter.NewSongAdapter;
import com.example.musicapplication_zingmp3.Adapter.PersonalMusicAdapter;
import com.example.musicapplication_zingmp3.Model.Album;
import com.example.musicapplication_zingmp3.Model.MediaPlayerSingleton;
import com.example.musicapplication_zingmp3.Model.Song;
import com.example.musicapplication_zingmp3.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.ArrayList;

public class PersonalMusicFragment extends Fragment {
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    View view;
    RecyclerView recyclerViewPersonalSong;
    ArrayList<Song> personalSongs;
    ArrayList<Song> songs;
    Song song;
    ArrayList<String> trimmedSongIds;
    PersonalMusicAdapter personalMusicAdapter;
    RelativeLayout playerView;
    Button btnPlayAll;
    MediaPlayer mediaPlayer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_personal_music, container, false);
        ImageView searchIcon = getActivity().findViewById(R.id.searchIcon);
        Fragment currentFragment = ((AppCompatActivity)getContext()).getSupportFragmentManager().findFragmentById(R.id.fragmentLayout);
        if (currentFragment instanceof SearchFragment) {
            searchIcon.setImageResource(R.drawable.nav_menu_search_close);
        } else {
            searchIcon.setImageResource(R.drawable.nav_menu_search);
        }
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        recyclerViewPersonalSong = view.findViewById(R.id.recyclerViewPersonalSong);
        btnPlayAll = view.findViewById(R.id.btnPlayAll);
        mediaPlayer = MediaPlayerSingleton.getInstance().getMediaPlayer();
        playerView = getActivity().findViewById(R.id.playerView);
        personalSongs = new ArrayList<>();
        songs = new ArrayList<>();
        trimmedSongIds = new ArrayList<>();
        getSongLiked();
        eventClick();
        personalMusicAdapter = new PersonalMusicAdapter(getContext(),personalSongs,playerView);
        recyclerViewPersonalSong.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        recyclerViewPersonalSong.setAdapter(personalMusicAdapter);
        return view;
    }

    private void getSongLiked() {
        firebaseUser = firebaseAuth.getCurrentUser();
        String userId = firebaseUser.getUid().trim();

        DocumentReference docRef = firebaseFirestore.collection("Users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                ArrayList<String> songLiked = (ArrayList<String>) documentSnapshot.get("songLiked");
                for (String songId : songLiked) {
                    trimmedSongIds.add(songId.trim());
                }
                Log.d("songLiked",String.valueOf(songLiked));
                if(trimmedSongIds != null && !trimmedSongIds.isEmpty()){
                    firebaseFirestore.collection("Songs")
                            .whereIn("id", trimmedSongIds)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                        String id = document.getId().trim();
                                        String duration = document.getString("duration").trim();
                                        String image = document.getString("image").trim();
                                        String link = document.getString("link").trim();
                                        String title = document.getString("title").trim();
                                        String lyric = document.getString("lyric");
                                        int like = document.getLong("likes").intValue();
                                        Timestamp release = document.getTimestamp("release");
                                        String idAlbum = document.getString("idAlbum").trim();
                                        String idSinger = document.getString("idSinger").trim();
                                        String idBanner = document.getString("idBanner");
                                        Song song = new Song(id, duration, image, link, title, lyric, like, release, idAlbum, idSinger, idBanner);
                                        personalSongs.add(song);
                                    }
                                    personalMusicAdapter.notifyDataSetChanged();

                                } else {
                                    Log.d("No song found for user with id: ", firebaseUser.getUid().trim());
                                }
                            })
                            .addOnFailureListener(e -> Log.d("Error getting song for singer with id: "+ firebaseUser.getUid().trim(), e.getMessage()));
                }
            }
            else {Log.d("Error getting songLiked: ", userId);}
        }).addOnFailureListener(e -> Log.d("Error getting user: ", userId));
    }

    private void eventClick() {
        btnPlayAll.setOnClickListener(v -> {
            if (personalSongs.size() >= 2){
                Log.d("personalSongs", String.valueOf(personalSongs.size()));
                playSong(personalSongs.get(0));

                Intent intent = new Intent("personalSong");
                intent.putExtra("song", personalSongs.get(0));
                intent.putExtra("songs", personalSongs);
                getContext().sendBroadcast(intent);

            } else if (personalSongs.size() == 0){
                Toast.makeText(getContext(),"Không có bài nhạc nào để phát", Toast.LENGTH_LONG).show();
            } else {
                // Query Firestore for all songs
                firebaseFirestore.collection("Songs")
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                    String id = document.getId().trim();
                                    String duration = document.getString("duration").trim();
                                    String image = document.getString("image").trim();
                                    String link = document.getString("link").trim();
                                    String title = document.getString("title").trim();
                                    String lyric = document.getString("lyric");
                                    int like = document.getLong("likes").intValue();
                                    Timestamp release = document.getTimestamp("release");
                                    String idAlbum = document.getString("idAlbum").trim();
                                    String idSinger = document.getString("idSinger").trim();
                                    String idBanner = document.getString("idBanner");
                                    Song song = new Song(id, duration, image, link, title, lyric, like, release, idAlbum, idSinger, idBanner);

                                    songs.add(song);
                                }
                                playSong(personalSongs.get(0));
                                Intent intent = new Intent("personalSong");
                                intent.putExtra("song", personalSongs.get(0));
                                intent.putExtra("songs", songs);
                                getContext().sendBroadcast(intent);
                            } else {
                                Log.d("No song found", "Empty Firestore collection");
                            }
                        })
                        .addOnFailureListener(e -> Log.d("Error getting songs", e.getMessage()));
            }
        });
    }

    private void playSong(Song firstSong) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(firstSong.getLink());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}