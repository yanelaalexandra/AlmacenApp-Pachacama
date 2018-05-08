package almacen.pachacama.com.almacenapp.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import almacen.pachacama.com.almacenapp.DetailActivity;
import almacen.pachacama.com.almacenapp.R;
import almacen.pachacama.com.almacenapp.interfaces.ApiService;
import almacen.pachacama.com.almacenapp.models.Producto;
import almacen.pachacama.com.almacenapp.models.ResponseMessage;
import almacen.pachacama.com.almacenapp.singletons.ApiServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Alumno on 4/05/2018.
 */

public class ProductosAdapter extends RecyclerView.Adapter<ProductosAdapter.ViewHolder>  {

    private static final String TAG = ProductosAdapter.class.getSimpleName();

    private List<Producto> productos;

    private Activity activity;

    public ProductosAdapter(Activity activity){
        this.productos = new ArrayList<>();
        this.activity = activity;
    }

    public void setProductos(List<Producto> productos){
        this.productos = productos;
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView fotoImage;
        public TextView nombreText;
        public TextView precioText;
        public ImageButton menuButton;

        public ViewHolder(View itemView) {
         super(itemView);
            fotoImage = (ImageView) itemView.findViewById(R.id.foto_image);
            nombreText = (TextView) itemView.findViewById(R.id.nombre_text);
            precioText = (TextView) itemView.findViewById(R.id.precio_text);
            menuButton = (ImageButton) itemView.findViewById(R.id.menu_button);

        }
    }

    @NonNull
    @Override
    public ProductosAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductosAdapter.ViewHolder viewHolder, final int position) {
      final Producto producto = this.productos.get(position);

        viewHolder.nombreText.setText(producto.getNombre());
        viewHolder.precioText.setText("S/. " + producto.getPrecio());

        String url = ApiService.API_BASE_URL + "/images/" + producto.getImagen();
        Picasso.with(viewHolder.itemView.getContext()).load(url).into(viewHolder.fotoImage);



        viewHolder.menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.remove_button:

                                ApiService service = ApiServiceGenerator.createService(ApiService.class);

                                Call<ResponseMessage> call = service.destroyProducto(producto.getId());

                                call.enqueue(new Callback<ResponseMessage>() {
                                    @Override
                                    public void onResponse(Call<ResponseMessage> call, Response<ResponseMessage> response) {
                                        try {

                                            int statusCode = response.code();
                                            Log.d(TAG, "HTTP status code: " + statusCode);

                                            if (response.isSuccessful()) {

                                                ResponseMessage responseMessage = response.body();
                                                Log.d(TAG, "responseMessage: " + responseMessage);

                                                // Eliminar item del recyclerView y notificar cambios
                                                productos.remove(position);
                                                notifyItemRemoved(position);
                                                notifyItemRangeChanged(position, productos.size());

                                                Toast.makeText(v.getContext(), responseMessage.getMessage(), Toast.LENGTH_LONG).show();

                                            } else {
                                                Log.e(TAG, "onError: " + response.errorBody().string());
                                                throw new Exception("Error en el servicio");
                                            }

                                        } catch (Throwable t) {
                                            try {
                                                Log.e(TAG, "onThrowable: " + t.toString(), t);
                                                Toast.makeText(v.getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                                            }catch (Throwable x){}
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseMessage> call, Throwable t) {
                                        Log.e(TAG, "onFailure: " + t.toString());
                                        Toast.makeText(v.getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                                    }

                                });

                                break;
                        }
                        return false;
                    }
                });
                popup.show();
            }
        });

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, DetailActivity.class);
                intent.putExtra("ID", producto.getId());
                activity.startActivity(intent);
            }
        });


    }





    @Override
    public int getItemCount() {
        return this.productos.size();
    }


}
