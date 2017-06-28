package md.lama.rbms.consumer.rbms.views.features.cart.presenter;

import com.android.volley.VolleyError;

import net.eunjae.android.modelmapper.ModelMapper;

import org.json.JSONException;
import org.json.JSONObject;

import md.lama.rbms.consumer.rbms.api.models.CategoryItemMapping;
import md.lama.rbms.consumer.rbms.listeners.OnDataReceivedListener;
import md.lama.rbms.consumer.rbms.views.features.cart.CartModel;
import md.lama.rbms.consumer.rbms.views.features.cart.model.CartProduct;
import md.lama.rbms.consumer.rbms.views.features.cart.model.CartProducts;
import md.lama.rbms.consumer.rbms.models.MenuModel;
import md.lama.rbms.consumer.rbms.utils.Constants;
import md.lama.rbms.consumer.rbms.utils.ErrorParser;
import md.lama.rbms.consumer.rbms.utils.SingletonSharedPreferences;
import md.lama.rbms.consumer.rbms.base.BasePresenter;


public class UpdateCartItemDialogPresenterImpl  extends BasePresenter
{
    private CartModel cartModel;
    private MenuModel menuModel;
    private OnDataReceivedListener<CategoryItemMapping> onCategoryItemMappingItemReceivedListener;

    public UpdateCartItemDialogPresenterImpl(OnDataReceivedListener<CategoryItemMapping> onCategoryItemMappingItemReceivedListener)
    {
        this.onCategoryItemMappingItemReceivedListener = onCategoryItemMappingItemReceivedListener;
        cartModel = new CartModel();
        menuModel = new MenuModel(this,this);
    }

    public void updateCartProduct(CartProduct cartProduct)
    {
        cartModel.updateCartProduct(cartProduct);
        menuModel = new MenuModel(this,this);
    }

    public void getCategoryItemMapping(CartProduct cartProduct)
    {
        onCategoryItemMappingItemReceivedListener.onStartReceiving();

        headers.clear();
        headers.put(Constants.NetworkKeys.AUTHORIZATION,SingletonSharedPreferences.getInstance().getToken());
        headers.put(Constants.NetworkKeys.CONTENT_TYPE,Constants.NetworkKeys.APP_JSON);

        CartProducts cartProducts = cartModel.getCartProducts();

        String restaurantLabel = cartProducts.getRestaurant().getLabel();
        String brandLabel = cartProducts.getBrandLabel();

        if (cartProduct.isComplex())
        {
            menuModel.getComplexCategoryItemMappingByUuid(restaurantLabel,brandLabel,cartProduct.getCategoryItemUuid(),headers);
        }
        else
        {
            menuModel.getSimpleCategoryItemMappingByUuid(restaurantLabel,brandLabel,cartProduct.getCategoryItemUuid(),headers);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error)
    {
        if (!isSubscribed)
            return;

        ErrorParser errorParser = new ErrorParser(error);
        onCategoryItemMappingItemReceivedListener.onError(errorParser.getErrorDetails());
    }

    @Override
    public void onResponse(JSONObject response)
    {
        if (!isSubscribed)
            return;

        try
        {
            CategoryItemMapping categoryItemMapping = (CategoryItemMapping) ModelMapper.getInstance().generate(CategoryItemMapping.class, response.toString());

            onCategoryItemMappingItemReceivedListener.onReceived(categoryItemMapping);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
