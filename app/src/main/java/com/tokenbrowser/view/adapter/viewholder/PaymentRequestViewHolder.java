/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tokenbrowser.view.adapter.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tokenbrowser.R;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.model.sofa.PaymentRequest;
import com.tokenbrowser.util.EthUtil;
import com.tokenbrowser.util.ImageUtil;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;

public final class PaymentRequestViewHolder extends RecyclerView.ViewHolder {

    private @NonNull TextView title;
    private @NonNull TextView ethereumAmount;
    private @NonNull TextView body;

    private @Nullable LinearLayout buttonWrapper;
    private @Nullable Button acceptButton;
    private @Nullable Button declineButton;
    private @Nullable ImageView avatar;
    private @Nullable TextView remotePaymentStatus;

    private OnItemClickListener<Integer> onApproveListener;
    private OnItemClickListener<Integer> onRejectListener;

    private PaymentRequest request;
    private String avatarUri;
    private User remoteUser;

    public PaymentRequestViewHolder(final View v) {
        super(v);
        this.title = (TextView) v.findViewById(R.id.title);
        this.ethereumAmount = (TextView) v.findViewById(R.id.eth_amount);
        this.body = (TextView) v.findViewById(R.id.body);
        this.buttonWrapper = (LinearLayout) v.findViewById(R.id.button_wrapper);
        this.acceptButton = (Button) v.findViewById(R.id.approve_button);
        this.declineButton = (Button) v.findViewById(R.id.reject_button);
        this.avatar = (ImageView) v.findViewById(R.id.avatar);
        this.remotePaymentStatus = (TextView) v.findViewById(R.id.remote_payment_status);
    }

    public PaymentRequestViewHolder setPaymentRequest(final PaymentRequest request) {
        this.request = request;
        return this;
    }

    public PaymentRequestViewHolder setAvatarUri(final String uri) {
        this.avatarUri = uri;
        return this;
    }

    public PaymentRequestViewHolder setRemoteUser(final User remoteUser) {
        this.remoteUser = remoteUser;
        return this;
    }

    public PaymentRequestViewHolder setOnApproveListener(final OnItemClickListener<Integer> onApproveListener) {
        this.onApproveListener = onApproveListener;
        return this;
    }

    public PaymentRequestViewHolder setOnRejectListener(final OnItemClickListener<Integer> onRejectListener) {
        this.onRejectListener = onRejectListener;
        return this;
    }

    public void draw() {
        renderAmounts();
        renderBody();
        renderAvatar();
        renderStatus();
    }

    private void renderAmounts() {
        final String requestTitle = BaseApplication.get().getString(R.string.request_for_value, this.request.getLocalPrice());
        this.title.setText(requestTitle);
        final String ethAmount = String.format(
                BaseApplication.get().getResources().getString(R.string.eth_amount),
                EthUtil.hexAmountToUserVisibleString(this.request.getValue()));
        this.ethereumAmount.setText(ethAmount);
    }

    private void renderBody() {
        final String body = this.request.getBody();
        this.body.setVisibility(View.GONE);
        if (body == null || body.length() == 0) return;
        this.body.setVisibility(View.VISIBLE);
        this.body.setText(body);
    }

    private void renderAvatar() {
        if (this.avatar == null) return;
        ImageUtil.load(this.avatarUri, this.avatar);
    }

    private void renderStatus() {
        final @PaymentRequest.State int state = this.request.getState();
        switch (state) {
            case PaymentRequest.ACCEPTED:
                renderAcceptStatusMessage();
                break;
            case PaymentRequest.REJECTED:
                renderRejectStatusMessage();
                break;
            case PaymentRequest.PENDING:
            default:
                renderPendingStatusMessage();
                break;
        }
    }

    private void renderAcceptStatusMessage() {
        if (!isSendByRemote() || this.remotePaymentStatus == null || this.buttonWrapper == null) return;
        this.buttonWrapper.setVisibility(View.GONE);
        this.remotePaymentStatus.setVisibility(View.VISIBLE);
        final String acceptMessage = BaseApplication.get().getString(R.string.you_accepted);
        this.remotePaymentStatus.setText(acceptMessage);
    }

    private void renderRejectStatusMessage() {
        if (!this.isSendByRemote() || this.remotePaymentStatus == null || this.buttonWrapper == null) return;
        this.buttonWrapper.setVisibility(View.GONE);
        this.remotePaymentStatus.setVisibility(View.VISIBLE);
        final String rejectMessage = BaseApplication.get().getString(R.string.you_declined);
        this.remotePaymentStatus.setText(rejectMessage);
    }

    private void renderPendingStatusMessage() {
        if (!this.isSendByRemote() || this.buttonWrapper == null || this.remotePaymentStatus == null) return;
        this.remotePaymentStatus.setVisibility(View.GONE);
        this.buttonWrapper.setVisibility(View.VISIBLE);
        this.declineButton.setOnClickListener(__ -> handleRejectedClicked());
        this.acceptButton.setOnClickListener(__ -> handleApprovedClicked());
    }

    private boolean isSendByRemote() {
        if (this.remoteUser == null || this.request == null) return false;
        return this.request.getDestinationAddresss().equals(this.remoteUser.getPaymentAddress());
    }

    private void handleApprovedClicked() {
        if (this.onApproveListener == null) return;
        this.onApproveListener.onItemClick(getAdapterPosition());
    }

    private void handleRejectedClicked() {
        if (this.onRejectListener == null) return;
        this.onRejectListener.onItemClick(getAdapterPosition());
    }
}
