package md.lama.rbms.consumer.rbms.views.features.cart.model;

import net.eunjae.android.modelmapper.annotation.JsonProperty;

import java.util.ArrayList;

import md.lama.rbms.consumer.rbms.api.models.BasicRestaurantInfo;

public class CartProducts
{
    @JsonProperty("cartProducts")
    private ArrayList<CartProduct> cartProducts = new ArrayList<>();
    @JsonProperty("restaurantUuid")
    private String restaurantUuid;
    @JsonProperty("brandLabel")
    private String brandLabel;
    @JsonProperty("restaurant")
    private BasicRestaurantInfo restaurant;
    @JsonProperty("currency")
    private String currency;

    public CartProducts()
    {

    }

    public CartProducts(String restaurantUuid)
    {
        this.restaurantUuid = restaurantUuid;
    }

    public CartProducts(BasicRestaurantInfo restaurant, String brandLabel, String currency)
    {
        this.restaurant = restaurant;
        this.brandLabel = brandLabel;
        this.currency = currency;
    }

    public CartProducts(BasicRestaurantInfo restaurant, String brandLabel, String currency,ArrayList<CartProduct> cartProducts)
    {
        this.restaurant = restaurant;
        this.brandLabel = brandLabel;
        this.currency = currency;
        this.cartProducts = cartProducts;
    }

    public void addProduct(CartProduct cartProduct)
    {
        cartProducts.add(cartProduct);
    }

    public void setCartProducts(ArrayList<CartProduct> cartProducts)
    {
        this.cartProducts = cartProducts;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrency()
    {
        return currency;
    }

    public ArrayList<CartProduct> getCartProductsList()
    {
        return cartProducts;
    }

    public int getCartAmount()
    {
        if (cartProducts != null)
            return cartProducts.size();
        else
            return 0;
    }

    public double geTotalPrice()
    {
        double price = 0;

        for(CartProduct cartProduct:cartProducts)
        {
            price += (cartProduct.getPrice());
        }

        return price;
    }

    public BasicRestaurantInfo getRestaurant()
    {
        return restaurant;
    }

    public String getBrandLabel()
    {
        return brandLabel;
    }

    public String getRestaurantUuid()
    {
        return restaurant.getUuid();
    }
}
