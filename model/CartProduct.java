package md.lama.rbms.consumer.rbms.views.features.cart.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import net.eunjae.android.modelmapper.annotation.JsonProperty;
import java.util.ArrayList;

import md.lama.rbms.consumer.rbms.api.models.Image;
import md.lama.rbms.consumer.rbms.api.models.Product;
import md.lama.rbms.consumer.rbms.utils.Constants;
import md.lama.rbms.consumer.rbms.utils.Utils;


public class CartProduct implements Parcelable
{
    @JsonProperty("uuid")
    private String uuid;
    @JsonProperty("image")
    private Image image;
    @JsonProperty("title")
    private String title;
    @JsonProperty("price")
    private int price;
    @JsonProperty("category_item_uuid")
    private String category_item_uuid;
    @JsonProperty(Constants.JsonKeys.QUANTITY)
    private int quantity = 0;
    @JsonProperty("selected_products")
    private ArrayList<Product> selected_products;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("is_complex")
    private boolean is_complex;

    public CartProduct() {}

    public CartProduct(String currency, boolean is_complex, ArrayList<Product> products, Image image, String title, String category_item_uuid, int price, int quantity)
    {
        uuid = Utils.generateUuid();
        selected_products = products;
        this.is_complex = is_complex;
        this.currency = currency;
        this.price = price;
        this.image = image;
        this.title = title;
        this.category_item_uuid = category_item_uuid;
        this.quantity = quantity;
    }

    public ArrayList<Product> getSelectedProducts()
    {
        if (selected_products != null)
        {
            return selected_products;
        }
        else
        {
            selected_products = new ArrayList<>();
            return selected_products;
        }
    }


    public String getCurrency() {
        return currency;
    }

    public boolean isComplex()
    {
        return is_complex;
    }

    public Image getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public String getUuid() {
        return uuid;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setSelectedProducts(@NonNull ArrayList<Product> selected_products)
    {
        this.selected_products = selected_products;
    }

    public String getCategoryItemUuid() {
        return category_item_uuid;
    }

    public int getPrice()
    {
        int price = this.price;

        if (selected_products != null)
        {
            for (Product product:selected_products)
            {
                price += product.getPrice();
            }
        }

        price *= quantity;

        return price;
    }




    protected CartProduct(Parcel in) {
        uuid = in.readString();
        image = in.readParcelable(Image.class.getClassLoader());
        title = in.readString();
        price = in.readInt();
        category_item_uuid = in.readString();
        quantity = in.readInt();
        selected_products = in.createTypedArrayList(Product.CREATOR);
        currency = in.readString();
        is_complex = in.readByte() != 0;
    }

    public static final Creator<CartProduct> CREATOR = new Creator<CartProduct>() {
        @Override
        public CartProduct createFromParcel(Parcel in) {
            return new CartProduct(in);
        }

        @Override
        public CartProduct[] newArray(int size) {
            return new CartProduct[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(uuid);
        parcel.writeParcelable(image, i);
        parcel.writeString(title);
        parcel.writeInt(price);
        parcel.writeString(category_item_uuid);
        parcel.writeInt(quantity);
        parcel.writeTypedList(selected_products);
        parcel.writeString(currency);
        parcel.writeByte((byte) (is_complex ? 1 : 0));
    }
}
