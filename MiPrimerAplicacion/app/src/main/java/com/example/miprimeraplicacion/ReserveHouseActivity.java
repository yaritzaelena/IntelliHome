package com.example.miprimeraplicacion;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReserveHouseActivity extends AppCompatActivity {
    private String checkInDate;
    private String checkOutDate;
    private String houseId;  // ✅ Agregar variable para almacenar el ID de la casa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve_house);

        // ✅ Obtener el ID de la casa desde la intención
        houseId = getIntent().getStringExtra("HOUSE_ID");

        Button buttonSelectDates = findViewById(R.id.buttonSelectDates);
        Button buttonConfirmReservation = findViewById(R.id.buttonConfirmReservation);

        buttonSelectDates.setOnClickListener(v -> showDateRangePicker());
        buttonConfirmReservation.setOnClickListener(v -> sendReservationRequest());
    }

    private void showDateRangePicker() {
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now());

        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Selecciona tu fecha de reserva");
        builder.setCalendarConstraints(constraintsBuilder.build());

        MaterialDatePicker<Pair<Long, Long>> datePicker = builder.build();
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");

        datePicker.addOnPositiveButtonClickListener((MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>) selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            checkInDate = sdf.format(new Date(selection.first));
            checkOutDate = sdf.format(new Date(selection.second));

            ((TextView) findViewById(R.id.textViewSelectedDates))
                    .setText("Entrada: " + checkInDate + "  |  Salida: " + checkOutDate);
        });
    }

    private void sendReservationRequest() {
        if (houseId == null || checkInDate == null || checkOutDate == null) {
            // ⚠️ Validación para asegurarse de que los datos están completos
            ((TextView) findViewById(R.id.textViewSelectedDates))
                    .setText("Error: Falta información para la reserva.");
            return;
        }

        // ✅ Enviar la información al servidor
        MainActivity.reserveHouse(houseId, checkInDate, checkOutDate, new MainActivity.LoginResponseCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> ((TextView) findViewById(R.id.textViewSelectedDates))
                        .setText("Reserva confirmada: " + checkInDate + " - " + checkOutDate));
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> ((TextView) findViewById(R.id.textViewSelectedDates))
                        .setText("Error en la reserva: " + error));
            }
        });
    }
}
