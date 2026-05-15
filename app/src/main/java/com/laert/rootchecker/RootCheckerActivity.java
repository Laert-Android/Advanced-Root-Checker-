package com.laert.rootchecker;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class RootCheckerActivity extends Activity {

    private LinearLayout resultsLayout;
    private TextView summaryText;
    private Button scanButton;
    private ProgressBar progressBar;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainHandler = new Handler(Looper.getMainLooper());
        buildUI();
    }

    private void buildUI() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0D1117"));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 48, 32, 48);

        TextView title = new TextView(this);
        title.setText("Advanced Root Checker");
        title.setTextSize(22f);
        title.setTextColor(Color.parseColor("#58A6FF"));
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("Open-source  No Ads  No Tracking");
        sub.setTextSize(11f);
        sub.setTextColor(Color.parseColor("#8B949E"));
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 4, 0, 32);
        root.addView(sub);

        summaryText = new TextView(this);
        summaryText.setText("Tap SCAN to begin root detection.");
        summaryText.setTextColor(Color.parseColor("#C9D1D9"));
        summaryText.setTextSize(14f);
        summaryText.setGravity(Gravity.CENTER);
        summaryText.setPadding(24, 20, 24, 20);
        summaryText.setBackgroundColor(Color.parseColor("#161B22"));
        root.addView(summaryText);

        root.addView(makeSpacer(16));

        progressBar = new ProgressBar(this, null,
            android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.INVISIBLE);
        root.addView(progressBar);
        root.addView(makeSpacer(8));

        scanButton = new Button(this);
        scanButton.setText("SCAN FOR ROOT");
        scanButton.setTextColor(Color.WHITE);
        scanButton.setBackgroundColor(Color.parseColor("#238636"));
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScan();
            }
        });
        root.addView(scanButton);
        root.addView(makeSpacer(24));

        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#30363D"));
        LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 1);
        root.addView(divider, divLp);
        root.addView(makeSpacer(16));

        TextView resultsLabel = new TextView(this);
        resultsLabel.setText("CHECK RESULTS");
        resultsLabel.setTextColor(Color.parseColor("#8B949E"));
        resultsLabel.setTextSize(11f);
        resultsLabel.setTypeface(Typeface.DEFAULT_BOLD);
        resultsLabel.setPadding(0, 0, 0, 12);
        root.addView(resultsLabel);

        resultsLayout = new LinearLayout(this);
        resultsLayout.setOrientation(LinearLayout.VERTICAL);
        root.addView(resultsLayout);

        root.addView(makeSpacer(32));

        TextView footer = new TextView(this);
        footer.setText("All checks run locally. No data is sent anywhere.");
        footer.setTextColor(Color.parseColor("#6E7681"));
        footer.setTextSize(11f);
        footer.setGravity(Gravity.CENTER);
        root.addView(footer);

        scroll.addView(root);
        setContentView(scroll);
    }

    private void startScan() {
        scanButton.setEnabled(false);
        scanButton.setText("Scanning...");
        resultsLayout.removeAllViews();
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        summaryText.setText("Running checks...");
        summaryText.setTextColor(Color.parseColor("#C9D1D9"));
        summaryText.setBackgroundColor(Color.parseColor("#161B22"));

        Thread t = new Thread(new Runnable() {
            public void run() {
                RootDetector detector = new RootDetector();
                final RootDetector.CheckResult[] checks = detector.runAllChecks();
                final int total = checks.length;
                int rootCount = 0;

                for (int i = 0; i < checks.length; i++) {
                    final int progress = (int)(((float)(i + 1) / total) * 100);
                    final RootDetector.CheckResult check = checks[i];
                    if (check.detected) rootCount++;

                    mainHandler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progress);
                            addResultRow(check);
                        }
                    });

                    try {
                        Thread.sleep(120);
                    } catch (InterruptedException e) {
                        break;
                    }
                }

                final int detected = rootCount;
                mainHandler.post(new Runnable() {
                    public void run() {
                        progressBar.setVisibility(View.INVISIBLE);
                        scanButton.setEnabled(true);
                        scanButton.setText("SCAN AGAIN");
                        updateSummary(detected, total);
                    }
                });
            }
        });
        t.start();
    }

    private void addResultRow(RootDetector.CheckResult check) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(16, 14, 16, 14);
        row.setBackgroundColor(Color.parseColor("#161B22"));

        TextView icon = new TextView(this);
        if (check.detected) {
            icon.setText("[!] ");
            icon.setTextColor(Color.parseColor("#F85149"));
        } else {
            icon.setText("[OK] ");
            icon.setTextColor(Color.parseColor("#3FB950"));
        }
        icon.setTextSize(12f);
        icon.setTypeface(Typeface.DEFAULT_BOLD);
        row.addView(icon);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);

        TextView name = new TextView(this);
        name.setText(check.name);
        name.setTextColor(Color.parseColor("#C9D1D9"));
        name.setTextSize(13f);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        info.addView(name);

        TextView detail = new TextView(this);
        detail.setText(check.detail);
        detail.setTextColor(Color.parseColor("#6E7681"));
        detail.setTextSize(11f);
        info.addView(detail);

        row.addView(info);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 4);
        resultsLayout.addView(row, lp);
    }

    private void updateSummary(int detected, int total) {
        if (detected == 0) {
            summaryText.setText("CLEAN - No root indicators found (" + total + " checks passed)");
            summaryText.setTextColor(Color.parseColor("#3FB950"));
            summaryText.setBackgroundColor(Color.parseColor("#0D1F17"));
        } else if (detected <= 2) {
            summaryText.setText("WARNING - " + detected + " of " + total + " checks suggest possible root");
            summaryText.setTextColor(Color.parseColor("#D29922"));
            summaryText.setBackgroundColor(Color.parseColor("#1A1400"));
        } else {
            summaryText.setText("ROOTED - " + detected + " of " + total + " checks indicate ROOT");
            summaryText.setTextColor(Color.parseColor("#F85149"));
            summaryText.setBackgroundColor(Color.parseColor("#1F0D0D"));
        }
    }

    private View makeSpacer(int dp) {
        View v = new View(this);
        float density = getResources().getDisplayMetrics().density;
        v.setMinimumHeight((int)(dp * density));
        return v;
    }
}
