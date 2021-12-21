package androidsamples.java.tictactoe;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LoginFragment extends Fragment {

    private EditText mEditEmail;
    private EditText mEditPassword;
    private Button mLogInBtn;

    private FirebaseAuth mAuth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Navigation.findNavController(requireView()).navigate(R.id.action_login_successful);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mEditEmail = view.findViewById(R.id.edit_email);
        mEditPassword = view.findViewById(R.id.edit_password);
        mLogInBtn = view.findViewById(R.id.btn_log_in);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLogInBtn.setOnClickListener(this::signInOrRegisterHandler);
    }

    /**
     * log in to the game using firebase
     * @param v the register/signin button view
     */
    private void signInOrRegisterHandler(View v) {
        String email = mEditEmail.getText().toString().trim();
        String password = mEditPassword.getText().toString().trim();

        if (email.length() == 0 || password.length() == 0) {
            Toast.makeText(getContext(), "Enter Details!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if(!task.isSuccessful()) {
                try {
                    throw Objects.requireNonNull(task.getException());
                } catch(FirebaseAuthInvalidUserException e) {
                    userSignIn(v, email, password);
                } catch (FirebaseAuthWeakPasswordException e) {
                    Toast.makeText(getContext(), "Weak Password!", Toast.LENGTH_SHORT).show();
                } catch(FirebaseAuthInvalidCredentialsException e) {
                    Toast.makeText(getContext(), "Invalid Credentials!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Server Error!", Toast.LENGTH_SHORT).show();
                }
            } else {
                NavDirections action = LoginFragmentDirections.actionLoginSuccessful();
                Navigation.findNavController(v).navigate(action);
            }
        });
    }

    /**
     * sign the user in if the user is already registered
     * @param v the sign in button view
     * @param email user email
     * @param password user password
     */
    private void userSignIn(View v, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                try {
                    throw Objects.requireNonNull(task.getException());
                } catch (FirebaseAuthWeakPasswordException e) {
                    Toast.makeText(getContext(), "Weak Password!", Toast.LENGTH_SHORT).show();
                } catch(Exception e) {
                    Toast.makeText(getContext(), "Server Error!", Toast.LENGTH_SHORT).show();
                }
            } else {
                NavDirections action = LoginFragmentDirections.actionLoginSuccessful();
                Navigation.findNavController(v).navigate(action);
            }
        });
    }

    // No options menu in login fragment.
}