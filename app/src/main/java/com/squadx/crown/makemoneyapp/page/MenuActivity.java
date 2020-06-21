package com.squadx.crown.makemoneyapp.page;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.shobhitpuri.custombuttons.GoogleSignInButton;
import com.squadx.crown.makemoneyapp.R;
import com.squadx.crown.makemoneyapp.controller.AppController;
import com.squadx.crown.makemoneyapp.controller.PreferenceController;
import com.squadx.crown.makemoneyapp.controller.ReactionController;
import com.squadx.crown.makemoneyapp.model.User;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MenuActivity extends AppCompatActivity {

    private final String TAG = MenuActivity.class.getName();
    private final int RC_SIGN_IN = 100;
    public static final int RC_MENU = 200;
    public static final String RC_REFRESH_REQUIRE = "REFRESH";

    @BindView(R.id.i_am_avatar)
    View mAvatarLayout;
    @BindView(R.id.gsib_am_sign_in)
    GoogleSignInButton mSignInBtn;
    @BindView(R.id.ll_am_logout)
    View mLogout;
    @BindView(R.id.iv_spal_image)
    ImageView mProfileIV;
    @BindView(R.id.tv_spal_name)
    TextView mDisplayName;
    @BindView(R.id.tv_spal_email)
    TextView mEmail;
    @BindView(R.id.pbar_am_loading)
    ProgressBar mLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ButterKnife.bind(this);

        updateMainMenu();
    }

    @OnClick(R.id.gsib_am_sign_in)
    void onClickedSignIn() {
        Intent signInIntent = AppController.getInstance().getGoogleSignInClient().getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @OnClick(R.id.ll_am_home)
    void onClickedHome() {
        finish();
    }

    @OnClick(R.id.ll_am_rate)
    void onClickedRate() {
        final String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    @OnClick(R.id.ll_am_recover)
    void onClickedRestoreArticle() {
        setActivityResult();
    }

    @OnClick(R.id.ll_am_share)
    void onClickedShare() {
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

    @OnClick(R.id.ll_am_contact_us)
    void onClickedContactUs() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.gmail)});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Make Money support: ");
        if (intent.resolveActivity(getPackageManager()) != null) startActivity(intent);
    }

    @OnClick(R.id.ll_am_logout)
    void onClickedLogOut() {
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
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (Exception e) {
                Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(@NonNull GoogleSignInAccount acct) {
        mLoading.setVisibility(View.VISIBLE);
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        AppController.getInstance().getFirebaseAuth().signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = AppController.getInstance().getFirebaseAuth().getCurrentUser();
                if (user != null) setUser(acct, user);
                else Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
            }
            mLoading.setVisibility(View.GONE);
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

            Glide.with(this).load(user.getProfileImage()).apply(new RequestOptions().centerCrop().circleCrop()).into(mProfileIV);
            mDisplayName.setText(user.getName());
            mEmail.setText(user.getEmail());

            mAvatarLayout.setVisibility(View.VISIBLE);
            mSignInBtn.setVisibility(View.GONE);
            mLogout.setVisibility(View.VISIBLE);
        } else {
            mAvatarLayout.setVisibility(View.GONE);
            mSignInBtn.setVisibility(View.VISIBLE);
            mLogout.setVisibility(View.GONE);
        }
    }
}