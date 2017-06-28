package md.lama.rbms.consumer.rbms.views.features.cart.presenter;

import com.android.volley.VolleyError;

import org.json.JSONObject;

import md.lama.rbms.consumer.rbms.api.models.BasicRestaurantInfo;
import md.lama.rbms.consumer.rbms.views.features.cart.CartModel;
import md.lama.rbms.consumer.rbms.views.features.cart.model.CartProducts;
import md.lama.rbms.consumer.rbms.base.BasePresenter;


public class CartPresenter extends BasePresenter
{
    private CartModel model;

    public CartPresenter()
    {
        model = new CartModel();
    }

    public CartProducts getProducts()
    {
        return model.getCartProducts();
    }

    public void onIncrement(String uuid)
    {
        model.incrementCartProductQuantity(uuid);
    }

    public void onDecrement(String uuid)
    {
        model.decrementCartProductQuantity(uuid);
    }

    public void emptyCart()
    {
        model.emptyCart();
    }

    public void onRemoveItem(String uuid)
    {
        model.removeProductItem(uuid);
    }

    public BasicRestaurantInfo getRestaurant()
    {
        return model.getRestaurant();
    }

    public String getBrandLabel()
    {
        return model.getBrandLabel();
    }


    @Override
    public void onErrorResponse(VolleyError error) {

        if (!isSubscribed)
            return;
    }

    @Override
    public void onResponse(JSONObject response) {

        if (!isSubscribed)
            return;


    }
}
