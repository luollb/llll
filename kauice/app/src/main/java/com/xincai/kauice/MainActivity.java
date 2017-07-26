package com.xincai.kauice;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.shenyuanqing.zxingsimplify.zxing.Activity.CaptureActivity;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    private Context mContext;
    private Activity mActivity;
    private static final int REQUEST_SCAN = 0;

    private TextView tv_content;

    private ArrayList<String> list;

    private ObjectAnimator animator;

    RequestQueue mRequestQueue;

    // 创建一个空的ContentValues
    ContentValues values;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    /**
     * 动画
     */
    private void startAnimator1() {
        animator = ObjectAnimator.ofFloat(iv, "rotation", 0, 360);
        animator.setDuration(1000);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.start();
    }

    private ImageView iv;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 1) {
                tv_content.setText(getContactCount() + "");
                iv.setVisibility(View.INVISIBLE);
                animator.cancel();
                Toast.makeText(MainActivity.this, "添加完成", Toast.LENGTH_LONG).show();
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        mActivity = this;

        init();

        tv_content.setText(getContactCount() + "");

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void postInfo(String name) {
        String url = "http://www.weitongbu.cn/data/" + name + ".txt";
        StringRequest mStringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Log.e("luo", "s = " + s);
                List<String> listPhone = getNumber2(s);
                dialog(listPhone);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(MainActivity.this, "网络错误", Toast.LENGTH_LONG).show();
            }
        });

        mRequestQueue.add(mStringRequest);
    }

    /**
     * 通过正则表达式判断号码
     *
     * @param content
     * @return
     */
    public ArrayList getNumber2(String content) {
        list = new ArrayList();
        Pattern p = Pattern.compile("\\d{11}");
        Matcher matcher = p.matcher(content);
        while (matcher.find()) {
            String n = matcher.group(0).toString();
            list.add(n);
        }
        return list;
    }

    /**
     * 添加联系人
     *
     * @param name
     * @param phoneNumber
     */
    public void addContact(String name, String phoneNumber) {

        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        ContentResolver resolver = getContentResolver();
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        ContentProviderOperation op1 = ContentProviderOperation.newInsert(uri)
                .withValue("account_name", null)
                .build();
        operations.add(op1);

        uri = Uri.parse("content://com.android.contacts/data");
        ContentProviderOperation op2 = ContentProviderOperation.newInsert(uri)
                .withValueBackReference("raw_contact_id", 0)
                .withValue("mimetype", "vnd.android.cursor.item/name")
                .withValue("data2", name)
                .build();
        operations.add(op2);

        ContentProviderOperation op3 = ContentProviderOperation.newInsert(uri)
                .withValueBackReference("raw_contact_id", 0)
                .withValue("mimetype", "vnd.android.cursor.item/phone_v2")
                .withValue("data1", phoneNumber)
                .withValue("data2", "2")
                .build();
        operations.add(op3);

        try {
            resolver.applyBatch("com.android.contacts", operations);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取本机联系人数
     *
     * @return
     */
    private int getContactCount() {
        Cursor c = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, new String[]{ContactsContract.Contacts._COUNT}, null, null, null);
        try {
            c.moveToFirst();
            return c.getInt(0);
        } catch (Exception e) {
            return 0;
        } finally {
            c.close();
        }
    }

    /**
     * 获得运行时权限
     */
    private void getRuntimeRight() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            jumpScanPage();
        }
    }

    /**
     * 跳转到扫码页
     */
    private void jumpScanPage() {
        startActivityForResult(new Intent(MainActivity.this, CaptureActivity.class), REQUEST_SCAN);
    }

    /**
     * 获取返回结果
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SCAN && resultCode == RESULT_OK) {
            String data1 = data.getStringExtra("barCode");
            postInfo(data1);

        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。

            File file = new File(uri.getPath());
            String datas = readString(file.getAbsolutePath(), "utf-8");
            if (datas != null) {
                List<String> listPhone = getNumber2(datas);
                dialog(listPhone);
            } else {
                Toast.makeText(MainActivity.this, "数据为空请从新导入", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 从文件中读取数据
     *
     * @param file
     * @return
     */
    public static byte[] readBytes(String file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            int len = fis.available();
            byte[] buffer = new byte[len];
            fis.read(buffer);
            fis.close();
            return buffer;
        } catch (Exception e) {
            Log.e("luo", e.getMessage());
        }
        return null;
    }

    /**
     * 从文件中读取数据，返回类型是字符串String类型
     *
     * @param file    文件路径
     * @param charset 读取文件时使用的字符集，如utf-8、GBK等
     * @return
     */
    public String readString(String file, String charset) {
        byte[] data = readBytes(file);
        String ret = null;

        try {
            ret = new String(data, charset);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return ret;
    }

    /**
     * 清空联系人
     *
     * @throws Exception
     */
    public void testDelete() throws Exception {
        String name = "xxx";
        //根据姓名求id
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        ContentResolver resolver = this.getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{ContactsContract.Contacts.Data._ID}, "display_name like ?",
                new String[]{"%" + name + "%"}, null);
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            //根据id删除data中的相应数据
            resolver.delete(uri, "display_name like ?", new String[]{"%" + name + "%"});
            uri = Uri.parse("content://com.android.contacts/data");
            resolver.delete(uri, "raw_contact_id=?", new String[]{id + ""});
        }
    }

    private void init() {

        // 创建一个空的ContentValues
        values = new ContentValues();

        mRequestQueue = Volley.newRequestQueue(this);

        iv = (ImageView) findViewById(R.id.iv);

        tv_content = (TextView) findViewById(R.id.tv_content);

        //扫描
        findViewById(R.id.bt_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRuntimeRight();
            }
        });

        //本地文件
        findViewById(R.id.button_openfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 2);
            }
        });

        //清空联系人
        findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dialog1();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //使用有任何疑问点我咨询
        findViewById(R.id.but_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //派生到我的代码片
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=" + "3113476564" + "&version=1")));
            }
        });
    }

    /**
     * 弹出对话框 导入联系人
     *
     * @param listPhone
     */
    private void dialog(final List<String> listPhone) {
        Dialog dialog = new AlertDialog.Builder(this)
                .setMessage("确认导入" + listPhone.size() + "个号码")
                .setTitle("提示")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, "开始添加联系人", Toast.LENGTH_LONG).show();

                        iv.setVisibility(View.VISIBLE);
                        startAnimator1();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < listPhone.size(); i++) {
                                    addContact("xxx-" + listPhone.get(i).toString(), listPhone.get(i).toString() + "");
                                }
                                Message msg = handler.obtainMessage();
                                msg.what = 1;
                                handler.sendMessage(msg);
                            }
                        }).start();
                    }
                })
                .create();
        dialog.show();
    }

    /**
     * 弹出对话框 清空联系人
     */
    private void dialog1() {
        Dialog dialog = new AlertDialog.Builder(this)
                .setMessage("清空联系人(不能清空请询问客服)")
                .setTitle("提示")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, "正在清除联系人", Toast.LENGTH_LONG).show();

                        iv.setVisibility(View.VISIBLE);
                        startAnimator1();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                try {
                                    testDelete();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                Message msg = handler.obtainMessage();
                                msg.what = 1;
                                handler.sendMessage(msg);
                            }
                        }).start();

                        Toast.makeText(MainActivity.this, "联系人清除成功", Toast.LENGTH_LONG).show();
                    }
                })
                .create();
        dialog.show();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
