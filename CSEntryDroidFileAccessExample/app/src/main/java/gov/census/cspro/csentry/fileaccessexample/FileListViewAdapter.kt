package gov.census.cspro.csentry.fileaccessexample

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import java.util.*

class FileListViewAdapter(private val context1: Context, private val values: ArrayList<String>) :
    ArrayAdapter<String>(
        context1, -1, values
    ) {

    public var selIdx  = -1

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.file_list_view_item, parent, false)
        //TextView textView = (TextView) rowView.findViewById(R.id.label);
        //ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        //textView.setText(values[position]);
        val radioButton = rowView.findViewById<View>(R.id.radioButton) as RadioButton
        radioButton.text = values[position]

        radioButton.isChecked = selIdx == position

        return rowView
    }

}