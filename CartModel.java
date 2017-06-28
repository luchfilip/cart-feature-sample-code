package md.lama.rbms.consumer.rbms.views.features.cart;

import android.util.Log;

import com.google.gson.Gson;
import java.util.ArrayList;

import md.lama.rbms.consumer.rbms.api.models.BasicRestaurantInfo;
import md.lama.rbms.consumer.rbms.api.models.CategoryItemMapping;
import md.lama.rbms.consumer.rbms.api.models.Image;
import md.lama.rbms.consumer.rbms.api.models.Product;
import md.lama.rbms.consumer.rbms.utils.Constants;
import md.lama.rbms.consumer.rbms.utils.SingletonSharedPreferences;
import md.lama.rbms.consumer.rbms.views.features.cart.model.CartProduct;
import md.lama.rbms.consumer.rbms.views.features.cart.model.CartProducts;


public class CartModel
{

    public CartModel()
    {
    }

    public int getCartProductCountByUuid(String uuid)
    {
        int count = 0;

        CartProducts cartProducts = getCartProducts();

        if (cartProducts != null)
        {
            for (CartProduct product:cartProducts.getCartProductsList())
            {
                if (product.getCategoryItemUuid().equals(uuid))
                    count = product.getQuantity();
            }
        }

        return count;
    }

    public int getCartProductCount()
    {
        return SingletonSharedPreferences.getInstance().getCartProductsCount();
    }

    public void updateCartProduct(CartProduct newCartProduct)
    {
        CartProducts cartProducts = getCartProducts();

        if (cartProducts != null)
        {
            for (CartProduct cartProduct:cartProducts.getCartProductsList())
            {
                if (newCartProduct.getUuid().equals(cartProduct.getUuid()))
                {
                    cartProduct.setQuantity(newCartProduct.getQuantity());
                    cartProduct.setSelectedProducts(newCartProduct.getSelectedProducts());
                }
            }

            saveCart(cartProducts);
        }

    }

    public void incrementCartProductQuantity(String uuid)
    {
        CartProducts cartProducts = getCartProducts();

        if (cartProducts != null)
        {
            for (CartProduct cartProduct:cartProducts.getCartProductsList())
            {
                if (cartProduct.getCategoryItemUuid().equals(uuid))
                {
                    cartProduct.setQuantity(cartProduct.getQuantity()+1);
                }
            }

            saveCart(cartProducts);
        }
    }

    public void decrementCartProductQuantity(String uuid)
    {
        CartProducts cartProducts = getCartProducts();

        if (cartProducts != null )
        {
            for (CartProduct cartProduct:cartProducts.getCartProductsList())
            {
                if (cartProduct.getCategoryItemUuid().equals(uuid))
                {
                    cartProduct.setQuantity(cartProduct.getQuantity()-1);
                }
            }

            saveCart(cartProducts);
        }

    }

    public boolean addProduct(CategoryItemMapping categoryItemMapping, int quantity, String brandLabel, BasicRestaurantInfo restaurant, String currency)
    {
        CartProducts cartProducts = getCartProducts();
        ArrayList<CartProduct> cartProductList = new ArrayList<>();
        CartProduct cartProduct;
        ArrayList<Product> products = new ArrayList<>();
        Image image = categoryItemMapping.getCategoryItem().getImage();
        String title = categoryItemMapping.getCategoryItem().getTitle();
        String categoryItemUuid = categoryItemMapping.getCategoryItem().getUuid();
        int price = categoryItemMapping.getCategoryItem().getPrice();
        boolean isComplex = categoryItemMapping.isComplex();

        /*
         * save the products if they are available within the category item
        */
        if (isComplex)
        {
            products = categoryItemMapping.getSelectedProducts();
        }

        /*
         * Check if there are no products in cart
        */
        if (cartProducts == null)
        {
            cartProduct = new CartProduct(currency,isComplex,products,image,title,categoryItemUuid,price,quantity);
            cartProductList.add(cartProduct);

            cartProducts = new CartProducts(restaurant,brandLabel,currency,cartProductList);

            saveCart(cartProducts);

            return true;
        }
        /*
         * Current cart is not empty
        */
        else
        {
            /*
             * Check if we add products from same restaurant
            */
            if (cartProducts.getRestaurantUuid().equals(restaurant.getUuid()))
            {
                cartProductList = cartProducts.getCartProductsList();

                for(CartProduct product:cartProductList)
                {
                    // check for same category items in cart
                    if (product.getCategoryItemUuid().equals(categoryItemMapping.getCategoryItem().getUuid()))
                    {
                        // check if same options were selected for same category items
                        if (isSameProductsCombination(categoryItemMapping.getSelectedProducts(),product.getSelectedProducts()))
                        {
                            // increase qty if we have same options selected
                            product.setQuantity(product.getQuantity()+quantity);
                        }
                        else
                        {
                            // create a new cart item with different options
                            cartProduct = new CartProduct(currency,isComplex,products,image,title,categoryItemUuid,price,quantity);

                            cartProductList.add(cartProduct);
                        }

                        break;
                    }
                }

                saveCart(cartProducts);

                return true;
            }
        }

        return false;
    }

    /*
     * Check if the options from two category items are the same 
    */
    private boolean isSameProductsCombination(ArrayList<Product> newProducts, ArrayList<Product> products)
    {
        boolean isSame = true;

        if (newProducts.size() != products.size())
        {
            isSame = false;
        }
        else
        {
            // iterate through options(products) from the category item that we want to add to cart
            for (Product newProduct:newProducts)
            {
                boolean isEqualUuid = false;

                /*
                 * iterate through products from the current 
                 * category item that we currently have in cart
                */
                for (Product product:products)
                {
                    // check products' UUID
                    if (newProduct.getUuid().equals(product.getUuid()))
                    {
                        isEqualUuid = true;
                    }
                }

                isSame = isEqualUuid;

                if (!isSame)
                {
                    break;
                }
            }
        }

        return isSame;
    }

    /*
     * Remove item from cart by UUID
    */
    public void removeProductItem(String uuid)
    {
        CartProducts cartProducts = getCartProducts();

        if (cartProducts != null)
        {
            ArrayList<CartProduct> cartProductList = cartProducts.getCartProductsList();

            for (int i=0;i<cartProductList.size();i++)
            {
                if (cartProductList.get(i).getCategoryItemUuid().equals(uuid))
                {
                    cartProductList.remove(i);
                }
            }

            saveCart(cartProducts);
        }

    }

    /*
     * Save the cart data to SharedPrefs
    */
    public void saveCart(CartProducts cartProducts)
    {
        String json;
        Gson gson = new Gson();

        ArrayList<CartProduct> cartProductList = cartProducts.getCartProductsList();

        json = gson.toJson(cartProducts);

        SingletonSharedPreferences.getInstance().putString(Constants.SharedPreferences.CART_CART_TAG,json);
        SingletonSharedPreferences.getInstance().putInteger(Constants.SharedPreferences.CART_PRODUCTS_COUNT_TAG, getCartProductCount(cartProductList));
    }

    public String getBrandLabel()
    {
        CartProducts cartProducts = getCartProducts();

        if (cartProducts != null)
            return cartProducts.getBrandLabel();
        else
            return null;
    }

    public BasicRestaurantInfo getRestaurant()
    {
        CartProducts cartProducts = getCartProducts();

        return cartProducts != null ? cartProducts.getRestaurant() : new BasicRestaurantInfo();
    }

    public int getCartProductCount(ArrayList<CartProduct> cartProducts)
    {
        int quantity =0;

        for (CartProduct cartProduct:cartProducts)
        {
            quantity += cartProduct.getQuantity();
        }

        return quantity;
    }

    public CartProducts getCartProducts()
    {
        String json = SingletonSharedPreferences.getInstance().getCartProducts();
        Gson gson = new Gson();

        return gson.fromJson(json, CartProducts.class);
    }

    /*
     * Remove all items from cart
    */
    public void emptyCart()
    {
        SingletonSharedPreferences.getInstance().emptyCart();
    }
}
