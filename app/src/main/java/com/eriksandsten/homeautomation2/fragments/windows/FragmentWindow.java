package com.eriksandsten.homeautomation2.fragments.windows;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import com.eriksandsten.homeautomation2.R;
import com.eriksandsten.homeautomation2.fragments.BaseFragment;
import com.eriksandsten.homeautomation2.utils.HomeAutomationUtils;

import lombok.Getter;

public abstract class FragmentWindow implements StandardWindowOperations {
    private final View view;
    private int windowId;
    private final View windowView;
    private final BaseFragment fragment;
    private int windowWidth;
    private int windowHeight;
    private int minimizedHeight;
    private final Button minimizeButton;
    private final Button maximizeButton;
    private final Button closeButton;

    public FragmentWindow(View view, int windowId, BaseFragment fragment, Integer windowWidth, Integer windowHeight, int minimizedHeight) {
        this.view = view;
        this.windowId = windowId;
        this.windowView = view.findViewById(windowId);
        this.fragment = fragment;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.minimizedHeight = minimizedHeight;
        this.minimizeButton = initButton(R.id.minimizeWindowBtn, fragment.getString(R.string.minimize), (__) -> onMinimizeButtonClick(view));
        this.maximizeButton = initButton(R.id.maximizeWindowBtn, fragment.getString(R.string.maximize), (__) -> onMaximizeButtonClickWithTransition(view,
                HomeAutomationUtils.convertPxToDp(fragment.getContext(), view.getMeasuredWidth()), HomeAutomationUtils.convertPxToDp(fragment.getContext(), view.getMeasuredHeight())));
        this.closeButton = initButton(R.id.closeWindowBtn, fragment.getString(R.string.close), (__) -> onCloseButtonClick(view));

        if (windowWidth != null && windowHeight != null) {
            view.findViewById(windowId).setLayoutParams(new FrameLayout.LayoutParams(
                HomeAutomationUtils.convertDpToPx(fragment.getContext(), windowWidth),
                HomeAutomationUtils.convertDpToPx(fragment.getContext(), windowHeight),
                Gravity.CENTER));
        }
    }

    @Override
    public void minimizeWindow() {
        view.findViewById(windowId).setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                HomeAutomationUtils.convertDpToPx(fragment.getContext(), minimizedHeight), Gravity.BOTTOM));
    }

    @Override
    public void maximizeWindow() {
        view.findViewById(windowId).setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
    }

    @Override
    public void closeWindow() {
        view.findViewById(windowId).setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideWindow() {
        view.findViewById(windowId).setVisibility(View.INVISIBLE);
    }

    @Override
    public void showWindow() {
        view.findViewById(windowId).setVisibility(View.VISIBLE);
    }

    protected Button initButton(int buttonId, String text, View.OnClickListener onClickCallback) {
        Button button = view.findViewById(buttonId);
        button.setText(text);
        button.setContentDescription(text);
        button.setOnClickListener(onClickCallback);

        return button;
    }

    protected void updateButtonText(Button button, String text) {
        button.setText(text);
        button.setContentDescription(text);
    }

    protected void onMinimizeButtonClick(View view) {
        final Context context = fragment.getContext();

        if (context.getString(R.string.minimize).equals(minimizeButton.getContentDescription())) {
            updateButtonText(minimizeButton, fragment.getString(R.string.restore));
            maximizeButton.setContentDescription(fragment.getString(R.string.maximize));
            view.findViewById(windowId).setLayoutParams(
                    new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            HomeAutomationUtils.convertDpToPx(context, minimizedHeight), Gravity.BOTTOM));
        } else {
            minimizeButton.setText(fragment.getString(R.string.minimize));
            maximizeButton.setText(fragment.getString(R.string.maximize));
            view.findViewById(windowId).setLayoutParams(new FrameLayout.LayoutParams(
                    HomeAutomationUtils.convertDpToPx(context, windowWidth),
                    HomeAutomationUtils.convertDpToPx(context, windowHeight),
                    Gravity.CENTER)
            );
        }
    }

    protected void onMaximizeButtonClick(Button maximizeButton, Button minimizeButton, View view) {
        final Context context = fragment.getContext();

        if (context.getString(R.string.maximize).equals(maximizeButton.getContentDescription())) {
            minimizeButton.setContentDescription(fragment.getString(R.string.minimize));
            maximizeButton.setContentDescription(fragment.getString(R.string.restore));
            maximizeWindow();
        } else {
            minimizeButton.setContentDescription(fragment.getString(R.string.minimize));
            maximizeButton.setContentDescription(fragment.getString(R.string.maximize));
            view.findViewById(R.id.videoListBrowserWindow).setLayoutParams(
                new FrameLayout.LayoutParams(
                    HomeAutomationUtils.convertDpToPx(context, windowWidth),
                    HomeAutomationUtils.convertDpToPx(context, windowHeight),
                    Gravity.CENTER)
                );
        }
    }

    protected void onMaximizeButtonClickWithTransition(View view, float maximizedWidth, float maximizedHeight) {
        final Context context = fragment.getContext();

        if (fragment.getString(R.string.maximize).equals(maximizeButton.getContentDescription())) {
            updateButtonText(minimizeButton, fragment.getString(R.string.minimize));
            updateButtonText(maximizeButton, fragment.getString(R.string.restore));

            // Get the current layout and its width and height
            final View windowView = view.findViewById(windowId);
            final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) windowView.getLayoutParams();

            // Define the target width and height for maximizing the window (full screen example)
            final int targetWidth = HomeAutomationUtils.convertDpToPx(context, maximizedWidth);  // Set this to the width you want when maximized
            final int targetHeight = HomeAutomationUtils.convertDpToPx(context, maximizedHeight);  // Set this to the width you want when maximized

            // Get the current width and height of the view
            final int initialWidth = layoutParams.width;
            final int initialHeight = layoutParams.height;

            // Create a ValueAnimator for width animation
            ValueAnimator widthAnimator = ValueAnimator.ofInt(initialWidth, targetWidth);
            widthAnimator.setDuration(250); // Set the duration of the animation (500ms)

            // Create a ValueAnimator for height animation
            ValueAnimator heightAnimator = ValueAnimator.ofInt(initialHeight, targetHeight);
            heightAnimator.setDuration(250); // Set the duration of the animation (500ms)

            // Add update listeners to update the layout width and height during the animation
            widthAnimator.addUpdateListener(animation -> {
                int animatedWidth = (int) animation.getAnimatedValue();
                layoutParams.width = animatedWidth; // Update the layout width
                windowView.setLayoutParams(layoutParams); // Apply the updated layout parameters
            });

            heightAnimator.addUpdateListener(animation -> {
                int animatedHeight = (int) animation.getAnimatedValue();
                layoutParams.height = animatedHeight; // Update the layout height
                windowView.setLayoutParams(layoutParams); // Apply the updated layout parameters
            });

            // Use AnimatorSet to play both animations together
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(widthAnimator, heightAnimator);
            animatorSet.start(); // Start the animations

        } else {
            updateButtonText(minimizeButton, fragment.getString(R.string.minimize));
            updateButtonText(maximizeButton, fragment.getString(R.string.maximize));

            // Get the current layout and its width and height
            final View videoListBrowserWindow = view.findViewById(R.id.videoListBrowserWindow);
            final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) videoListBrowserWindow.getLayoutParams();

            // Define the target width and height for restoring the window (smaller size)
            final int targetWidth = HomeAutomationUtils.convertDpToPx(context, windowWidth); // Smaller width for minimized state
            final int targetHeight = HomeAutomationUtils.convertDpToPx(context, minimizedHeight); // Smaller height for minimized state

            // Get the current width and height of the view
            final int initialWidth = layoutParams.width;
            final int initialHeight = layoutParams.height;

            // Create a ValueAnimator for width animation
            ValueAnimator widthAnimator = ValueAnimator.ofInt(initialWidth, targetWidth);
            widthAnimator.setDuration(250); // Set the duration of the animation (500ms)

            // Create a ValueAnimator for height animation
            ValueAnimator heightAnimator = ValueAnimator.ofInt(initialHeight, targetHeight);
            heightAnimator.setDuration(250); // Set the duration of the animation (500ms)

            // Add update listeners to update the layout width and height during the animation
            widthAnimator.addUpdateListener(animation -> {
                int animatedWidth = (int) animation.getAnimatedValue();
                layoutParams.width = animatedWidth; // Update the layout width
                videoListBrowserWindow.setLayoutParams(layoutParams); // Apply the updated layout parameters
            });

            heightAnimator.addUpdateListener(animation -> {
                int animatedHeight = (int) animation.getAnimatedValue();
                layoutParams.height = animatedHeight; // Update the layout height
                videoListBrowserWindow.setLayoutParams(layoutParams); // Apply the updated layout parameters
            });

            // Use AnimatorSet to play both animations together
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(widthAnimator, heightAnimator);
            animatorSet.start(); // Start the animations
        }
    }

    protected void onCloseButtonClick(View view) {
        hideWindow();
    }
}
