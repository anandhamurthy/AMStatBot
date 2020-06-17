package com.amstatbot;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.amstatbot.Models.Chat;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.annotations.NotNull;

import java.util.List;

import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_IDLE;
import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE;
import static androidx.recyclerview.widget.ItemTouchHelper.RIGHT;

public class SwipeGraph extends ItemTouchHelper.Callback {

    private Drawable imageDrawable;
    //private Drawable shareRound;
    private RecyclerView.ViewHolder currentItemViewHolder;
    private View mView;
    private float dX = 0f;
    private float replyButtonProgress = 0f;
    private long lastReplyButtonAnimationTime = 0;
    private boolean swipeBack = false;
    private boolean isVibrate = false;
    private boolean startTracking = false;
    private float density;
    private final Context context;
    private final SwipeControllerActions swipeControllerActions;
    List<Chat> List;


    public SwipeGraph(@NotNull Context context, List<Chat> chatList, @NotNull SwipeControllerActions swipeControllerActions) {
        super();
        this.context = context;
        this.swipeControllerActions = swipeControllerActions;
        this.density = 1.0F;
        this.List = chatList;
    }


    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        mView = viewHolder.itemView;
        imageDrawable = context.getDrawable(R.drawable.graph);
//        shareRound = context.getDrawable(R.drawable.graph);
        return ItemTouchHelper.Callback.makeMovementFlags(ACTION_STATE_IDLE, RIGHT);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        if (swipeBack) {
            swipeBack = false;
            return 0;
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        int position = viewHolder.getAdapterPosition();
        Chat chat = List.get(position);
        if (chat.getType().equals("stock")){
            if (actionState == ACTION_STATE_SWIPE) {
                setTouchListener(recyclerView, viewHolder);
            }

            if (mView.getTranslationX() < convertTodp(130) || dX < this.dX) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                this.dX = dX;
                startTracking = true;
            }
            currentItemViewHolder = viewHolder;
            drawReplyButton(c, chat.getWebsite());
        }

    }


    private void setTouchListener(RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder) {
        recyclerView.setOnTouchListener((__, event) -> {
                swipeBack = event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP;
                if (swipeBack) {
                    if (Math.abs(mView.getTranslationX()) >= this.convertTodp(100)) {
                        swipeControllerActions.showReplyUI(viewHolder.getAdapterPosition());
                    }
                }
            return false;
        });
    }


    private void drawReplyButton(Canvas canvas, String website) {
        if (currentItemViewHolder == null) {
            return;
        }
        float translationX = mView.getTranslationX();
        long newTime = System.currentTimeMillis();
        long dt = Math.min(17, newTime - lastReplyButtonAnimationTime);
        lastReplyButtonAnimationTime = newTime;
        boolean showing = translationX >= convertTodp(30);
        if (showing) {
            if (replyButtonProgress < 1.0f) {
                replyButtonProgress += dt / 180.0f;
                if (replyButtonProgress > 1.0f) {
                    replyButtonProgress = 1.0f;
                } else {
                    mView.invalidate();
                }
            }
        } else if (translationX <= 0.0f) {
            replyButtonProgress = 0f;
            startTracking = false;
            isVibrate = false;
        } else {
            if (replyButtonProgress > 0.0f) {
                replyButtonProgress -= dt / 180.0f;
                if (replyButtonProgress < 0.1f) {
                    replyButtonProgress = 0f;
                } else {
                    mView.invalidate();
                }
            }
        }
        int alpha;
        float scale;
        if (showing) {
            scale = this.replyButtonProgress <= 0.8F ? 1.2F * (this.replyButtonProgress / 0.8F) : 1.2F - 0.2F * ((this.replyButtonProgress - 0.8F) / 0.2F);
            alpha = (int) Math.min(255.0F, (float) 255 * (this.replyButtonProgress / 0.8F));
        } else {
            scale = this.replyButtonProgress;
            alpha = (int) Math.min(255.0F, (float) 255 * this.replyButtonProgress);
        }
        //shareRound.setAlpha(alpha);
        imageDrawable.setAlpha(alpha);
        if (startTracking) {
            if (!isVibrate && mView.getTranslationX() >= convertTodp(100)) {
                Intent intent = new Intent(context, WebViewActivity.class);
                intent.putExtra("link",website);
                context.startActivity(intent);
                isVibrate = true;
            }
        }

        int x;
        if (mView.getTranslationX() > (float) this.convertTodp(130)) {
            x = this.convertTodp(130) / 2;
        } else {
            x = (int) (mView.getTranslationX() / (float) 2);
        }


        float y;
        y = (float) ((mView.getTop() + mView.getMeasuredHeight() / 2));
        //shareRound.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.colorAccent), PorterDuff.Mode.MULTIPLY));
        //shareRound.setBounds((int) ((float) x - (float) this.convertTodp(18) * scale), (int) (y - (float) this.convertTodp(18) * scale), (int) ((float) x + (float) this.convertTodp(18) * scale), (int) (y + (float) this.convertTodp(18) * scale));


        //shareRound.draw(canvas);
        imageDrawable.setBounds((int) ((float) x - (float) this.convertTodp(12) * scale), (int) (y - (float) this.convertTodp(11) * scale), (int) ((float) x + (float) this.convertTodp(12) * scale), (int) (y + (float) this.convertTodp(10) * scale));

        imageDrawable.draw(canvas);
        //shareRound.setAlpha(255);
        imageDrawable.setAlpha(255);
    }


    private final int convertTodp(int pixel) {
        return this.dp((float) pixel, this.context);
    }


    public int dp(Float value, Context context) {
        if (this.density == 1.0F) {
            this.checkDisplaySize(context);
        }

        return value == 0.0F ? 0 : (int) Math.ceil((double) (this.density * value));
    }

    private final void checkDisplaySize(Context context) {
        try {
            this.density = context.getResources().getDisplayMetrics().density;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public interface SwipeControllerActions {
        void showReplyUI(int var1);
    }

}