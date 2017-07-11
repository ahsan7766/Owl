package com.ourwayoflife.owl.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ourwayoflife.owl.R;

/**
 * Created by Zach on 7/11/17.
 */

public class ProfileEditDialogFragment extends AppCompatDialogFragment {

    /* The activity that creates an instance of this dialog fragment must
         * implement this interface in order to receive event callbacks.
         * Each method passes the DialogFragment in case the host needs to query it. */
    public interface ProfileEditDialogListener {
        void onDialogPositiveClick(ProfileEditDialogFragment dialog);

        void onDialogNegativeClick(ProfileEditDialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    ProfileEditDialogListener mListener;

    private EditText mEditTextName;
    private EditText mEditTextEmail;
    private EditText mEditTextBio;

    private String mName;
    private String mEmail;
    private String mBio;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host fragment implements the callback interface
        try {
            // Instantiate the ProfileEditDialogListener so we can send events to the host
            mListener = (ProfileEditDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            // The fragment doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement ProfileEditDialogListener");
        }
    }


    @Override
    public
    @NonNull
    Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog and set up the button click handlers
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate the view
        final View view = inflater.inflate(R.layout.dialog_edit_profile, null);

        mEditTextName = view.findViewById(R.id.edit_text_name);
        mEditTextName.setText(mName);

        mEditTextEmail = view.findViewById(R.id.edit_text_email);
        mEditTextEmail.setText(mEmail);

        mEditTextBio = view.findViewById(R.id.edit_text_bio);
        mEditTextBio.setText(mBio);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);

        // Set message and action buttons
        builder.setMessage(R.string.edit_profile)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mName = mEditTextName.getText().toString();
                        mEmail = mEditTextEmail.getText().toString();
                        mBio = mEditTextBio.getText().toString();

                        // Send the positive button event back to the host activity
                        mListener.onDialogPositiveClick(ProfileEditDialogFragment.this);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mName = mEditTextName.getText().toString();
                        mEmail = mEditTextEmail.getText().toString();
                        mBio = mEditTextBio.getText().toString();

                        // Send the negative button event back to the host activity
                        mListener.onDialogNegativeClick(ProfileEditDialogFragment.this);
                    }
                });
        return builder.create();
    }


    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getBio() {
        return mBio;
    }

    public void setBio(String bio) {
        mBio = bio;
    }
}
