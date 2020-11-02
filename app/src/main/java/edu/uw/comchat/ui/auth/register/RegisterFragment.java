package edu.uw.comchat.ui.auth.register;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import org.json.JSONException;
import org.json.JSONObject;

import edu.uw.comchat.databinding.FragmentRegisterBinding;


/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment {

  private FragmentRegisterBinding mBinding;

  private RegisterViewModel mRegisterModel;

  public RegisterFragment(){

  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mRegisterModel = new ViewModelProvider(getActivity())
            .get(RegisterViewModel.class);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment

    mBinding = FragmentRegisterBinding.inflate(inflater);
    return mBinding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mBinding.buttonAccept.setOnClickListener(button -> handleAcceptButton());
    mRegisterModel.addResponseObserver(
            getViewLifecycleOwner(), this::observeResponse);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    mBinding = null;
  }

  /**
   * Provide behavior when the Accept register button is pressed.
   */
  private void handleAcceptButton() {

    // TODO add verification

    this.verifyAuthWithServer();
  }

  private void verifyAuthWithServer() {
    // Step 67.
    mRegisterModel.connect(
            mBinding.editTextFirstName.getText().toString(),
            mBinding.editTextLastName.getText().toString(),
            mBinding.editTextEmail.getText().toString(),
            mBinding.editTextPassword.getText().toString());
    //This is an Asynchronous call. No statements after should rely on the
    //result of connect().
  }

  /**
   * An observer on the HTTP Response from the web server. This observer should be
   * attached to SignInViewModel.
   *
   * @param response the Response from the server
   */
  private void observeResponse(final JSONObject response) {
    Log.i("JSON body", response.toString());
    if (response.length() > 0) {
      if (response.has("code")) {
        try {
          mBinding.editTextEmail.setError(
                  "Error Authenticating: " +
                          response.getJSONObject("data").getString("message"));
        } catch (JSONException e) {
          Log.e("JSON Parse Error", e.getMessage());
        }
      } else {
//        navigateToLogin();
      }
    } else {
      Log.d("JSON Response", "No Response");
    }
  }

  private void navigateToLogin(){
    Navigation.findNavController(getView()).navigate(
            RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
    );
  }
}