package edu.uw.comchat;

import android.app.Activity;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.function.BiConsumer;

import edu.uw.comchat.databinding.ActivityMainBinding;
import edu.uw.comchat.model.NewMessageCountViewModel;
import edu.uw.comchat.model.UserInfoViewModel;
import edu.uw.comchat.services.PushReceiver;
import edu.uw.comchat.ui.chat.chatroom.ChatMessage;
import edu.uw.comchat.ui.chat.chatroom.ChatViewModel;
import edu.uw.comchat.util.UpdateTheme;

import static edu.uw.comchat.util.UpdateTheme.*;

/**
 * This class is a main activity for the program (homepage/weather/connection/chat/).
 */
// Minor adjustment so we use dialog instead of a whole fragment - Hung Vu
// Ignore checkstyle member name error.
public class MainActivity extends AppCompatActivity {
  private AppBarConfiguration mAppBarConfiguration;

  private final String RED_THEME = "red";
  private final String DEFAULT_THEME = "default";
  private final String GREY_THEME = "grey";

  private MainPushMessageReceiver mPushMessageReceiver;

  private NewMessageCountViewModel mNewMessageModel;

  private ActivityMainBinding mBinding;

  private static final BiConsumer<String, MainActivity> changeThemeHandler = updateThemeColor();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    //We need to set theme each activity before it is created
    setTheme(Theme.getTheme());
    //or we can recreate activity
    super.onCreate(savedInstanceState);
    mBinding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(mBinding.getRoot());


    BottomNavigationView navView = findViewById(R.id.nav_main_bottom_view);

    // Store email and jwt upon creation - Hung Vu.
    MainActivityArgs args = MainActivityArgs.fromBundle(getIntent().getExtras());
    String email = args.getEmail();
    String jwt = args.getJwt();
    new ViewModelProvider(
            this,
            new UserInfoViewModel.UserInfoViewModelFactory(email, jwt))
            .get(UserInfoViewModel.class); // First time initialize using inner factory method.

    // Passing each menu ID as a set of Ids because each
    // menu should be considered as top level destinations.
    mAppBarConfiguration = new AppBarConfiguration.Builder(
            R.id.navigation_home, R.id.navigation_weather,
            R.id.navigation_connection, R.id.navigation_chat)
            .build();

    // Get nav controller
    NavController navController = Navigation.findNavController(this, R.id.fragment_container_main);

    // Setup toolbar
    NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

    // Setup bottom nav
    NavigationUI.setupWithNavController(navView, navController);

    mNewMessageModel = new ViewModelProvider(this).get(NewMessageCountViewModel.class);

    navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
      if (destination.getId() == R.id.navigation_chat) {
        //When the user navigates to the chats page, reset the new message count.
        //This will need some extra logic for your project as it should have
        //multiple chat rooms.
        mNewMessageModel.reset();
      }
    });

    mNewMessageModel.addMessageCountObserver(this, count -> {
      BadgeDrawable badge = mBinding.navMainBottomView.getOrCreateBadge(R.id.navigation_chat);
      badge.setMaxCharacterCount(2);
      if (count > 0) {
        //new messages! update and show the notification badge.
        badge.setNumber(count);
        badge.setVisible(true);
      } else {
        //user did some action to clear the new messages, remove the badge
        badge.clearNumber();
        badge.setVisible(false);
      }
    });
  }

  @Override
  public void onResume() {
    super.onResume();
    if (mPushMessageReceiver == null) {
      mPushMessageReceiver = new MainPushMessageReceiver();
    }
    IntentFilter iFilter = new IntentFilter(PushReceiver.RECEIVED_NEW_MESSAGE);
    registerReceiver(mPushMessageReceiver, iFilter);
  }

  @Override
  public void onPause() {
    super.onPause();
    if (mPushMessageReceiver != null){
      unregisterReceiver(mPushMessageReceiver);
    }
  }

  /**
   * A BroadcastReceiver that listens for messages sent from PushReceiver
   */
  private class MainPushMessageReceiver extends BroadcastReceiver {

    private ChatViewModel mModel =
            new ViewModelProvider(MainActivity.this)
                    .get(ChatViewModel.class);

    @Override
    public void onReceive(Context context, Intent intent) {
      NavController nc =
              Navigation.findNavController(
                      MainActivity.this, R.id.fragment_container_main);
      NavDestination nd = nc.getCurrentDestination();

      if (intent.hasExtra("chatMessage")) {

        ChatMessage cm = (ChatMessage) intent.getSerializableExtra("chatMessage");

        //If the user is not on the chat screen, update the
        // NewMessageCountView Model
        if (nd.getId() != R.id.navigation_chat) {
          mNewMessageModel.increment();
        }
        //Inform the view model holding chatroom messages of the new
        //message.
        mModel.addMessage(intent.getIntExtra("chatid", -1), cm);
      }
    }
  }

  @Override
  public boolean onSupportNavigateUp() {
    NavController navController = Navigation.findNavController(this, R.id.fragment_container_main);
    return NavigationUI.navigateUp(navController, mAppBarConfiguration)
            || super.onSupportNavigateUp();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.toolbar, menu);
    return true;
  }


  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    int id = item.getItemId();

//    if (id == R.id.action_settings) {
//      NavController navController = Navigation.findNavController(
//              this, R.id.fragment_container_main);
//      navController.navigate(R.id.navigation_settings);
//    }
    if (id == R.id.menu_profile) {
      // This can be changed to a profile page. Just mapping setting fragment
      //  here to show the button is working normally.
      NavController navController = Navigation.findNavController(
              this, R.id.fragment_container_main);
      navController.navigate(R.id.navigation_settings);
    } else if (id == R.id.menu_theme) {
      handleChangeThemeAction();
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void recreate() {
    Intent intent = new Intent(MainActivity.this, MainActivity.class);
    startActivity(intent);
    finish();
  }

  private void handleChangeThemeAction() {
    String[] themeOptions = new String[]{"Default", "Blue Grey", "Red Black"};
    MainActivity thisActivity = this;
    new MaterialAlertDialogBuilder(this)
            .setTitle("Theme Options")
            // Recreate activity = lose all info (still can backup using bundle).
            // Checked item is a default choice, can be stored in bundle too. - Hung Vu
            .setSingleChoiceItems(themeOptions, 0,
                    (dialog, which) -> {
                      if (which == 0) {
                        updateThemeColor().accept(DEFAULT_THEME, thisActivity);

                      } else if (which == 1) {
                        updateThemeColor().accept(GREY_THEME, thisActivity);
                        Log.i("Theme name", "true");

                      } else if (which == 2) {
                        updateThemeColor().accept(RED_THEME, thisActivity);

                      }
                    })
            //  Require to press accept button after having a choice, but not working.
//            .setPositiveButton(getResources().getString(R.string.item_menu_change_theme_accept),
//                    (dialog, which) -> {
//                      if (which == 0){
//                        updateThemeColor().accept(DEFAULT_THEME, thisActivity);
////                        changeTheme(DEFAULT_THEME);
//                      } else if (which == 1){
//                        updateThemeColor().accept(GREY_THEME, thisActivity);
//                        Log.i("Theme name", "true");
////                        changeTheme(GREY_THEME);
//                      } else if (which == 2){
//                        updateThemeColor().accept(RED_THEME, thisActivity);
////                        changeTheme(RED_THEME);
//                      }
//                    })
            .show();
  }

}