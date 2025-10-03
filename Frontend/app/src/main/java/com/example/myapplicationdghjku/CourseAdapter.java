package com.example.myapplicationdghjku;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

    private static final String TAG = "CourseAdapter";
    private List<Course> courses;

    public CourseAdapter(List<Course> courses) {
        this.courses = courses != null ? courses : new ArrayList<>();
        Log.d(TAG, "Initialized with " + this.courses.size() + " items");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        Log.d(TAG, "Created ViewHolder for viewType: " + viewType);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "Binding position: " + position);
        Course course = courses.get(position);
        holder.tvCourseId.setText(course.getCourseId());
        holder.tvName.setText(course.getName());
        holder.tvCredits.setText(String.valueOf(course.getCredits()));
        holder.tvFee.setText(String.format("%.2f VNĐ", course.getFee()));
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseId, tvName, tvCredits, tvFee;

        public ViewHolder(View itemView) {
            super(itemView);
            tvCourseId = itemView.findViewById(R.id.tv_course_id);
            tvName = itemView.findViewById(R.id.tv_name);
            tvCredits = itemView.findViewById(R.id.tv_credits);
            tvFee = itemView.findViewById(R.id.tv_fee);
        }
    }
}