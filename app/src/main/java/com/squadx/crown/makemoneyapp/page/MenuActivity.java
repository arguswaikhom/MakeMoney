package com.squadx.crown.makemoneyapp.page;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squadx.crown.makemoneyapp.R;
import com.squadx.crown.makemoneyapp.controller.AppController;
import com.squadx.crown.makemoneyapp.controller.PreferenceController;
import com.squadx.crown.makemoneyapp.controller.ReactionController;
import com.squadx.crown.makemoneyapp.databinding.ActivityMenuBinding;
import com.squadx.crown.makemoneyapp.model.User;

import java.util.Arrays;

// todo: use view binding
public class MenuActivity extends AppCompatActivity {

    public static final int RC_MENU = 200;
    public static final String RC_REFRESH_REQUIRE = "REFRESH";
    private final String TAG = MenuActivity.class.getName();
    private final int RC_SIGN_IN = 100;
    private ActivityMenuBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        updateMainMenu();

        binding.signInWithGoogleBtn.setOnClickListener(this::onClickedSignIn);
        binding.menuInclude.homeMenu.setOnClickListener(this::onClickedHome);
        binding.menuInclude.rateMenu.setOnClickListener(this::onClickedRate);
        binding.menuInclude.recoverMenu.setOnClickListener(this::onClickedRestoreArticle);
        binding.menuInclude.shareMenu.setOnClickListener(this::onClickedShare);
        binding.menuInclude.contactUsMenu.setOnClickListener(this::onClickedContactUs);
        binding.menuInclude.logoutMenu.setOnClickListener(this::onClickedLogOut);
    }

    void onClickedSignIn(View view) {
        Intent signInIntent = AppController.getInstance().getGoogleSignInClient().getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    void onClickedHome(View view) {
        finish();
    }

    void onClickedRate(View view) {
        final String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    void onClickedRestoreArticle(View view) {
        setActivityResult();
    }

    void onClickedShare(View view) {
        String text = getString(R.string.share_msg) + "\n\n" + getString(R.string.app_link);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");

        try {
            Uri imageUri = Uri.parse(MediaStore.Images.Media.insertImage(this.getContentResolver(), BitmapFactory.decodeResource(getResources(), R.drawable.logo), null, null));
            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        } catch (Exception e) {
            e.printStackTrace();
        }

        intent.putExtra(Intent.EXTRA_TEXT, text);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    void onClickedContactUs(View view) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.gmail)});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Make Money support: ");
        if (intent.resolveActivity(getPackageManager()) != null) startActivity(intent);
    }

    void onClickedLogOut(View view) {
        AppController.getInstance().signOut();
        setActivityResult();
    }

    private void setActivityResult() {
        PreferenceController.getInstance(getApplicationContext()).clearRemovedArticle();
        Intent intent = new Intent();
        intent.putExtra(RC_REFRESH_REQUIRE, true);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            Log.v(TAG, task.toString());
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                updateUI(account);
                Log.v(TAG, "Account: " + account);

                firebaseAuthWithGoogle(account);
            } catch (Exception e) {
                Log.v(TAG, "\n\nSign in failed");
                Log.v(TAG, e.toString());
                Log.v(TAG, "" + Arrays.toString(e.getStackTrace()));
                updateUI(null);

                e.printStackTrace();
                Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account == null) Toast.makeText(this, "No account", Toast.LENGTH_SHORT).show();
        else Toast.makeText(this, account.getEmail(), Toast.LENGTH_SHORT).show();
    }

    private void firebaseAuthWithGoogle(@NonNull GoogleSignInAccount acct) {
        binding.loadingPbar.setVisibility(View.VISIBLE);
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        Log.v(TAG, acct.toString());

        AppController.getInstance().getFirebaseAuth().signInWithCredential(credential).addOnCompleteListener(this, task -> {
            Log.v(TAG, "\n\nTask Sign in failed");
            Log.v(TAG, task.toString());
            if (task.isSuccessful()) {
                FirebaseUser user = AppController.getInstance().getFirebaseAuth().getCurrentUser();
                if (user != null) setUser(acct, user);
                else Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
            }
            binding.loadingPbar.setVisibility(View.GONE);
        });
    }

    private void setUser(GoogleSignInAccount account, FirebaseUser user) {
        User u = new User(user.getUid(), account.getDisplayName(), account.getEmail(), account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : "");
        PreferenceController.getInstance(getApplicationContext()).setUser(u);
        FirebaseFirestore.getInstance().collection(getString(R.string.col_user)).document(user.getUid()).set(u);
        updateMainMenu();

        ReactionController.syncWithFirestore(this, this::setActivityResult);
    }

    private void updateMainMenu() {
        if (AppController.getInstance().isAuthenticated()) {
            User user = PreferenceController.getInstance(getApplicationContext()).getUser();

            Glide.with(this).load(user.getProfileImage()).apply(new RequestOptions().centerCrop().circleCrop()).into(binding.avatarInclude.imageIv);
            binding.avatarInclude.nameTv.setText(user.getName());
            binding.avatarInclude.emailTv.setText(user.getEmail());

            binding.avatarInclude.getRoot().setVisibility(View.VISIBLE);
            binding.signInWithGoogleBtn.setVisibility(View.GONE);
            binding.menuInclude.logoutMenu.setVisibility(View.VISIBLE);
        } else {
            binding.avatarInclude.getRoot().setVisibility(View.GONE);
            // todo: make sign-in btn visible after fixing the sign-in issue
            // binding.signInWithGoogleBtn.setVisibility(View.VISIBLE);
            binding.signInWithGoogleBtn.setVisibility(View.GONE);
            binding.loadingPbar.setVisibility(View.GONE);
        }
    }
}