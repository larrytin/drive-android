package com.artifex.mupdf;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.activity.BaseActivity;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.core.Registration;
import com.goodow.realtime.json.JsonObject;

import java.io.File;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MuPDFActivity extends BaseActivity {
  private enum LinkState {
    DEFAULT, HIGHLIGHT, INHIBIT
  };

  private final int TAP_PAGE_MARGIN = 5;
  private MuPDFCore core;
  private String mFileName;
  private ReaderView mDocView;
  private View mButtonsView;
  private boolean mButtonsVisible;
  private EditText mPasswordView;
  private TextView mFilenameView;
  private TextView mPageNumberView;
  private ImageButton mCancelButton;
  private View tempView = null;

  private AlertDialog.Builder mAlertBuilder;// private SearchTaskResult mSearchTaskResult;
  private final LinkState mLinkState = LinkState.DEFAULT;
  private static float scale = 2.5f;

  private RelativeLayout layout = null;
  private ImageView mImageView;
  private Registration controlHandler;
  private String path;

  public void createUI(Bundle savedInstanceState) {
    if (core == null) {
      return;
    }
    mDocView = initReaderView();
    mDocView.setAdapter(new MuPDFPageAdapter(this, core));
    makeButtonsView();
    mFilenameView.setText(mFileName);

    mCancelButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
      }
    });

    SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
    mDocView.setDisplayedViewIndex(prefs.getInt("page" + mFileName, 0));

    if (savedInstanceState == null || !savedInstanceState.getBoolean("ButtonsHidden", false)) {
      showButtons();
    }

    layout = new RelativeLayout(this);
    layout.addView(mDocView);
    layout.setBackgroundResource(R.drawable.pdf_shape_tiled_background);
    LayoutInflater mInflater = LayoutInflater.from(this);
    View mView = mInflater.inflate(R.layout.include_player_back, null);
    mImageView = (ImageView) mView.findViewById(R.id.iv_act_favour_back);
    RelativeLayout.LayoutParams mLayoutParams =
        new RelativeLayout.LayoutParams(getResources().getDimensionPixelOffset(
            R.dimen.act_home_fun_width), getResources().getDimensionPixelOffset(
            R.dimen.act_home_fun_height));
    mLayoutParams.leftMargin =
        getResources().getDimensionPixelOffset(R.dimen.video_back_marginLeft);
    mLayoutParams.topMargin = getResources().getDimensionPixelOffset(R.dimen.video_back_marginTop);
    mView.setLayoutParams(mLayoutParams);

    mImageView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (core != null) {
          core.onDestroy();
        }
        core = null;
        saveOnDatabases();
        MuPDFActivity.this.finish();
      }
    });
    LinearLayout controler = new LinearLayout(this);
    RelativeLayout.LayoutParams params =
        new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
    params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
    controler.setLayoutParams(params);
    Button previous = new Button(this);
    Button next = new Button(this);
    Button zoomIn = new Button(this);
    Button zoomOut = new Button(this);
    previous
        .setTextSize(getResources().getDimensionPixelOffset(R.dimen.pdf_player_button_textSize));
    next.setTextSize(getResources().getDimensionPixelOffset(R.dimen.pdf_player_button_textSize));
    zoomIn.setTextSize(getResources().getDimensionPixelOffset(R.dimen.pdf_player_button_textSize));
    zoomOut.setTextSize(getResources().getDimensionPixelOffset(R.dimen.pdf_player_button_textSize));
    previous.setText("上一页");
    next.setText("下一页");
    zoomIn.setText("缩小");
    zoomOut.setText("放大");

    controler.addView(previous);
    controler.addView(next);
    controler.addView(zoomOut);
    controler.addView(zoomIn);

    previous.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mDocView.moveToPrevious();
      }
    });
    next.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mDocView.moveToNext();
      }
    });

    zoomIn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (scale > 1.0f) {
          scale = 1.0f;
        }
        if (scale > 0.0f) {
          scale -= 0.1;
          mDocView.zoom(scale);
        }
        mDocView.onSettle(null);
      }
    });
    zoomOut.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        // 解决首次进入无法放大
        if (scale == 2.5 || scale < 1.0f) {
          scale = 1.0f;
        }
        if (scale < 1.6f) {
          scale += 0.1;
          mDocView.zoom(scale);
        }
        mDocView.onSettle(null);
      }
    });

    layout.addView(controler);
    layout.addView(mView);
    setContentView(layout);
  }

  @SuppressWarnings("deprecation")
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    JsonObject jsonObject = (JsonObject) getIntent().getExtras().getSerializable("msg");
    path = jsonObject.getString("path");
    mAlertBuilder = new AlertDialog.Builder(this);
    if (core == null) {
      core = (MuPDFCore) getLastNonConfigurationInstance();
      if (savedInstanceState != null && savedInstanceState.containsKey("FileName")) {
        mFileName = savedInstanceState.getString("FileName");
      }
    }
    if (core == null) {
      if (new File(path).isFile()) {
        core = openFile(path);
      } else {
        // TODO:可能有问题
        Toast.makeText(this, R.string.pdf_file_no_exist, Toast.LENGTH_SHORT).show();
        return;
      }
      SearchTaskResult.set(null);
      if (core != null && core.needsPassword()) {
        requestPassword(savedInstanceState);
        return;
      }
    }
    if (core == null) {
      AlertDialog alert = mAlertBuilder.create();
      alert.setTitle("open filed");
      alert.setButton(AlertDialog.BUTTON_POSITIVE, "Dismiss",
          new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              finish();
            }
          });
      alert.show();
      return;
    }

    createUI(savedInstanceState);
    handleControlMessage(jsonObject);
  }

  @Override
  public void onDestroy() {
    if (core != null) {
      core.onDestroy();
    }
    core = null;
    super.onDestroy();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      if (core != null) {
        core.onDestroy();
      }
      core = null;
      saveOnDatabases();
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    if (mButtonsVisible) {
      hideButtons();
    } else {
      showButtons();
    }
    return super.onPrepareOptionsMenu(menu);
  }

  @SuppressWarnings("deprecation")
  @Override
  public Object onRetainNonConfigurationInstance() {
    MuPDFCore mycore = core;
    core = null;
    return mycore;
  }

  @Override
  public boolean onSearchRequested() {
    if (mButtonsVisible) {
      hideButtons();
    } else {
      showButtons();
    }
    return super.onSearchRequested();
  }

  public void requestPassword(final Bundle savedInstanceState) {
    mPasswordView = new EditText(this);
    mPasswordView.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
    mPasswordView.setTransformationMethod(new PasswordTransformationMethod());

    AlertDialog alert = mAlertBuilder.create();
    alert.setTitle("Enter Password");
    alert.setView(mPasswordView);
    alert.setButton(AlertDialog.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (core.authenticatePassword(mPasswordView.getText().toString())) {
          createUI(savedInstanceState);
        } else {
          requestPassword(savedInstanceState);
        }
      }
    });
    alert.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        finish();
      }
    });
    alert.show();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode >= 0) {
      mDocView.setDisplayedViewIndex(resultCode);
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    JsonObject jsonObject = (JsonObject) intent.getExtras().getSerializable("msg");
    String path = jsonObject.getString("path");
    if (!path.equals(this.path)) {
      // path相同则不重新加载,只进行设置
      if (new File(path).isFile()) {
        this.path = path;
        core = openFile(path);
        SearchTaskResult.set(null);
        createUI(null);
      } else {
        Toast.makeText(this, R.string.pdf_file_no_exist, Toast.LENGTH_SHORT).show();
        return;
      }
    }
    handleControlMessage(jsonObject);
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (mFileName != null && mDocView != null) {
      SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
      SharedPreferences.Editor edit = prefs.edit();
      edit.putInt("page" + mFileName, mDocView.getDisplayedViewIndex());
      edit.commit();
    }
    controlHandler.unregister();
  }

  @Override
  protected void onResume() {
    super.onResume();
    controlHandler =
        bus.registerLocalHandler(Constant.ADDR_CONTROL, new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            JsonObject msg = message.body();
            if (msg.has("path")) {
              // 包含path的时候返回,onNewIntent处理
              return;
            }
            handleControlMessage(msg);
          }
        });
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    if (mFileName != null && mDocView != null) {
      outState.putString("FileName", mFileName);
      SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
      SharedPreferences.Editor edit = prefs.edit();
      edit.putInt("page" + mFileName, mDocView.getDisplayedViewIndex());
      edit.commit();
    }

    if (!mButtonsVisible) {
      outState.putBoolean("ButtonsHidden", true);
    }
  }

  void hideButtons() {
    if (mButtonsVisible) {
      mButtonsVisible = false;
    }
  }

  void makeButtonsView() {
    mButtonsView = getLayoutInflater().inflate(R.layout.pdf_buttons, null);
    mFilenameView = (TextView) mButtonsView.findViewById(R.id.docNameText);
    mPageNumberView = (TextView) mButtonsView.findViewById(R.id.pageNumber);
    mCancelButton = (ImageButton) mButtonsView.findViewById(R.id.cancel);
    mPageNumberView.setVisibility(View.INVISIBLE);
  }

  void showButtons() {
    if (core == null) {
      return;
    }
    if (!mButtonsVisible) {
      mButtonsVisible = true;
      int index = mDocView.getDisplayedViewIndex();
      updatePageNumView(index);
    }
  }

  void updatePageNumView(int index) {
    if (core == null) {
      return;
    }
    mPageNumberView.setText(String.format("%d/%d", index + 1, core.countPages()));
  }

  private void handleControlMessage(JsonObject body) {
    if (body.has("page")) {
      JsonObject page = body.getObject("page");
      if (page.has("goTo")) {
        /*
         * goTo 指定页码的移动
         */
        if (mDocView != null) {
          mDocView.setDisplayedViewIndex((int) body.getNumber("goTo"));
          setContentView(layout);
        }
      } else if (page.has("move")) {
        /*
         * move 相对于当前页码的偏移量移动
         */
        if (mDocView != null) {
          int offset = (mDocView.getDisplayedViewIndex() + (int) body.getNumber("move"));
          mDocView.setDisplayedViewIndex(offset);
          setContentView(layout);
        }
      }
    } else if (body.has("zoomTo")) {
      /*
       * zoomTo 指定缩放数值,基数是1
       */
      if (mDocView != null) {
        scale = (float) body.getNumber("zoomTo");
        if (scale > 2.5f) {
          scale = 2.5f;
        }
        if (scale < 0.0f) {
          scale = 0.5f;
        }
        mDocView.scale(scale);
        mDocView.onSettle(null);
      }
    } else if (body.has("zoomBy")) {
      /*
       * zoomBy 指定缩放系数,基数是当前缩放值
       */
      if (mDocView != null) {
        float temp = mDocView.getmScale() * (float) body.getNumber("zoomBy");
        if (temp > 5f) {
          temp = 5f;
        }
        if (temp < 0.5f) {
          temp -= 0.5;
        }
        scale = temp;
        mDocView.scale(scale);
        mDocView.onSettle(null);
      }
    }
  }

  private ReaderView initReaderView() {
    return new ReaderView(this) {
      private boolean showButtonsDisabled;

      @Override
      public boolean onScaleBegin(ScaleGestureDetector d) {
        showButtonsDisabled = true;
        return super.onScaleBegin(d);
      }

      @Override
      public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (!showButtonsDisabled) {
          hideButtons();
        }
        return super.onScroll(e1, e2, distanceX, distanceY);
      }

      @Override
      public boolean onSingleTapUp(MotionEvent e) {
        if (e.getX() < super.getWidth() / TAP_PAGE_MARGIN) {
          super.moveToPrevious();
        } else if (e.getX() > super.getWidth() * (TAP_PAGE_MARGIN - 1) / TAP_PAGE_MARGIN) {
          super.moveToNext();
        } else if (!showButtonsDisabled) {
          int linkPage = -1;
          if (linkPage != -1) {
            mDocView.setDisplayedViewIndex(linkPage);
          } else {
            if (!mButtonsVisible) {
              showButtons();
            } else {
              hideButtons();
            }
          }
        }
        return super.onSingleTapUp(e);
      }

      @Override
      public boolean onTouchEvent(MotionEvent event) {
        scale = this.getmScale();
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
          showButtonsDisabled = false;
        }

        return super.onTouchEvent(event);
      }

      @Override
      protected void onChildSetup(int i, View v) {
        if (SearchTaskResult.get() != null && SearchTaskResult.get().pageNumber == i) {
          ((PageView) v).setSearchBoxes(SearchTaskResult.get().searchBoxes);
        } else {
          ((PageView) v).setSearchBoxes(null);
        }
        ((PageView) v).setLinkHighlighting(mLinkState == LinkState.HIGHLIGHT);
      }

      @Override
      protected void onMoveToChild(int i) {
        if (core == null) {
          return;
        }
        mPageNumberView.setText(String.format("%d/%d", i + 1, core.countPages()));
        if (SearchTaskResult.get() != null && SearchTaskResult.get().pageNumber != i) {
          SearchTaskResult.set(null);
          mDocView.resetupChildren();
        }
      }

      @Override
      protected void onNotInUse(View v) {
        ((PageView) v).releaseResources();
      }

      @Override
      protected void onSettle(final View v) {
        if (v != null) {
          tempView = v;
        }
        this.postDelayed(new Runnable() {
          @Override
          public void run() {
            if (tempView != null) {
              ((PageView) tempView).addHq();
            }
          }
        }, 300);
      }

      @Override
      protected void onUnsettle(View v) {
        ((PageView) v).removeHq();
      }
    };
  }

  private MuPDFCore openFile(String path) {
    int lastSlashPos = path.lastIndexOf('/');
    mFileName = new String(lastSlashPos == -1 ? path : path.substring(lastSlashPos + 1));
    try {
      core = new MuPDFCore(path);
    } catch (Exception e) {
      System.out.println(e);
      return null;
    }
    return core;
  }
}

class ProgressDialogX extends ProgressDialog {
  private boolean mCancelled = false;

  public ProgressDialogX(Context context) {
    super(context);
  }

  @Override
  public void cancel() {
    mCancelled = true;
    super.cancel();
  }

  public boolean isCancelled() {
    return mCancelled;
  }
}

class SearchTaskResult {
  static public SearchTaskResult get() {
    return singleton;
  }

  static public void set(SearchTaskResult r) {
    singleton = r;
  }

  public final String txt;
  public final int pageNumber;

  public final RectF searchBoxes[];

  static private SearchTaskResult singleton;

  SearchTaskResult(String _txt, int _pageNumber, RectF _searchBoxes[]) {
    txt = _txt;
    pageNumber = _pageNumber;
    searchBoxes = _searchBoxes;
  }
}
