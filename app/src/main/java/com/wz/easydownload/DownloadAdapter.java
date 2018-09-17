package com.wz.easydownload;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wz.easydownload.bean.FileInfo;
import com.wz.easydownload.service.DownloadService;

import java.util.List;

/**
 * @author HuangFusheng
 * @date 2018/9/17
 * @description
 */
public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.ViewHolder> {

    private Context mContext;
    private List<FileInfo> mFileList;

    public DownloadAdapter(Context context, List<FileInfo> list) {
        mContext = context;
        mFileList = list;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_down, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        final FileInfo fileInfo = mFileList.get(position);
        viewHolder.mTvFileName.setText(fileInfo.getFileName());
        viewHolder.mPbProgress.setMax(100);
        viewHolder.mPbProgress.setProgress((int) fileInfo.getFinished());
        viewHolder.mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //给Service传递参数 开始下载
                Intent intent = new Intent(mContext, DownloadService.class);
                intent.setAction(DownloadService.ACTION_START);
                intent.putExtra("fileInfo", fileInfo);
                mContext.startService(intent);
            }
        });

        viewHolder.mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //给Service传递参数 停止下载
                Intent intent = new Intent(mContext, DownloadService.class);
                intent.setAction(DownloadService.ACTION_STOP);
                intent.putExtra("fileInfo", fileInfo);
                mContext.startService(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFileList == null ? 0 : mFileList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTvFileName;
        private ProgressBar mPbProgress;
        private Button mBtnStart;
        private Button mBtnStop;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvFileName = itemView.findViewById(R.id.tv_item_title);
            mPbProgress = itemView.findViewById(R.id.pb_item_progress);
            mBtnStart = itemView.findViewById(R.id.btn_start);
            mBtnStop = itemView.findViewById(R.id.btn_stop);
        }
    }


    /**
     * 更新列表项中进度条
     */

    public void updateProgress(int id, int progress) {
        FileInfo fileInfo = mFileList.get(id);
        fileInfo.setFinished(progress);
        notifyDataSetChanged();
    }
}
