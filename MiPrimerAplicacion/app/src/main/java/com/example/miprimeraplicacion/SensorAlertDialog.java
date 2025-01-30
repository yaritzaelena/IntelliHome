package com.example.miprimeraplicacion;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.app.AlertDialog;

public class SensorAlertDialog extends DialogFragment {
    private static final String ARG_MESSAGE = "message";

    public static SensorAlertDialog newInstance(String message) {
        SensorAlertDialog fragment = new SensorAlertDialog();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        assert getArguments() != null;
        String message = getArguments().getString(ARG_MESSAGE);

        return new AlertDialog.Builder(requireActivity())
                .setTitle("⚠️ Alerta de Sensor ⚠️")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .create();
    }
}
