package br.senai.tcc.nursecare.telas.agendamento;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import java.util.Calendar;

import br.senai.tcc.nursecare.R;

public class DataFragment extends Fragment implements CalendarView.OnDateChangeListener {

    private Calendar calendar;
    private long data = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data, container, false);

        calendar = Calendar.getInstance();

        if (calendar.get(Calendar.HOUR_OF_DAY) == 23)
            calendar.add(Calendar.DAY_OF_MONTH, 1);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (data == 0)
            data = calendar.getTimeInMillis();

        CalendarView cvData = view.findViewById(R.id.cvData);
        cvData.setDate(data);
        cvData.setMinDate(calendar.getTimeInMillis());
        calendar.add(Calendar.MONTH, 1);
        cvData.setMaxDate(calendar.getTimeInMillis());

        cvData.setOnDateChangeListener(this);

        return view;
    }

    @Override
    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        data = calendar.getTimeInMillis();
    }

    public boolean validaForm() {
        return data > 0;
    }

    public long getData() {
        return data;
    }
}
