package cm.aptoide.pt.ethereumapiexample;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class PaySomethingFragment extends DialogFragment {

  private OnPaymentConfirmedListener listener;

  interface OnPaymentConfirmedListener {
    void onPaymentConfirmed();
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    listener = (OnPaymentConfirmedListener) context;
  }

  @Override
  public void onDetach() {
    listener = null;
    super.onDetach();
  }

  @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
    alertDialogBuilder.setTitle(R.string.buy_something);
    alertDialogBuilder.setMessage(R.string.are_you_sure);
    //null should be your on click listener
    alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        listener.onPaymentConfirmed();
      }
    });
    alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

      @Override public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });

    return alertDialogBuilder.create();
  }
}
