package md.lama.rbms.consumer.rbms.views.features.cart.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import md.lama.rbms.consumer.rbms.R;
import md.lama.rbms.consumer.rbms.api.models.CategoryItem;
import md.lama.rbms.consumer.rbms.api.models.CategoryItemMapping;
import md.lama.rbms.consumer.rbms.api.models.CategoryProductMapping;
import md.lama.rbms.consumer.rbms.api.models.Product;
import md.lama.rbms.consumer.rbms.listeners.OnDataReceivedListener;
import md.lama.rbms.consumer.rbms.listeners.OnItemClickListener;
import md.lama.rbms.consumer.rbms.views.features.cart.model.CartProduct;
import md.lama.rbms.consumer.rbms.presenters.adapters.ProductGroupsAdapter;
import md.lama.rbms.consumer.rbms.views.features.cart.presenter.UpdateCartItemDialogPresenterImpl;
import md.lama.rbms.consumer.rbms.utils.Constants;
import md.lama.rbms.consumer.rbms.utils.Utils;
import md.lama.rbms.consumer.rbms.views.custom_views.CircleImageView;


public class UpdateCartItemDialogFragment extends DialogFragment
{
    private ProductGroupsAdapter adapter;
    private CategoryItemMapping categoryItemMapping;
    private CategoryItem categoryItem;
    private CartProduct cartProduct;
    private int itemQuantity;
    private UpdateCartItemDialogPresenterImpl presenter;
    private Intent intent;
    private String currency;
    private Unbinder unbinder;

    @BindView(R.id.product_image_iv) CircleImageView image;
    @BindView(R.id.product_name_tv) TextView name;
    @BindView(R.id.description) TextView description;
    @BindView(R.id.price_tv) TextView price;
    @BindView(R.id.currency_tv) TextView currencyTv;
    @BindView(R.id.quantity_tv) TextView quantity;
    @BindView(R.id.product_groups_rv) RecyclerView productGroupsList;
    @BindView(R.id.loading_view_ll) LinearLayout loadingView;

    public static UpdateCartItemDialogFragment newInstance(CartProduct cartProduct,String currency)
    {
        Bundle args = new Bundle();

        UpdateCartItemDialogFragment fragment = new UpdateCartItemDialogFragment();

        args.putParcelable(Constants.Bundle.CART_PRODUCT_ITEM_TAG,cartProduct);
        args.putString(Constants.Bundle.CURRENCY,currency);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null)
        {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        presenter = new UpdateCartItemDialogPresenterImpl(onCategoryItemMappingItemReceivedListener);


        intent = new Intent();

        cartProduct = getArguments().getParcelable(Constants.Bundle.CART_PRODUCT_ITEM_TAG);
        currency = getArguments().getString(Constants.Bundle.CURRENCY);

        itemQuantity = cartProduct.getQuantity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View layout = inflater.inflate(R.layout.dialog_update_cart_item,container,false);
        unbinder = ButterKnife.bind(this,layout);

        if (getDialog().getWindow() != null)
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        setUpList();

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        presenter.getCategoryItemMapping(cartProduct);
        presenter.subscribe();
    }

    private void bindFragment()
    {
        Glide.with(getContext()).load(Utils.getPhotoUrl(categoryItem.getImage())).into(image);
        quantity.setText(String.valueOf(itemQuantity));
        price.setText(String.valueOf(cartProduct.getPrice()));
        currencyTv.setText(currency);
        description.setText(categoryItem.getDescription());
        name.setText(categoryItem.getTitle());
    }

    private void setUpList()
    {
        adapter = new ProductGroupsAdapter(currency,getContext(), onProductsGroupClickListener);

        productGroupsList.setAdapter(adapter);
        productGroupsList.setHasFixedSize(true);
    }

    private void setAdapterData()
    {
        for (Product selectedProduct:cartProduct.getSelectedProducts())
        {
            for (CategoryProductMapping categoryProductMapping:categoryItem.getCategoryProducts())
            {
                for (Product product:categoryProductMapping.getCategoryProduct().getProducts())
                {
                    if (selectedProduct.getUuid().equals(product.getUuid()))
                    {
                        product.setSelected(true);
                    }
                }
            }
        }

        adapter.setProductGroups(categoryItem.getCategoryProducts());
    }

    @OnClick(R.id.decr_quantity_view)
    public void decrementQuantity()
    {
        if (itemQuantity > 1)
        {
            itemQuantity--;
            quantity.setText(String.valueOf(itemQuantity));
            price.setText(String.valueOf(categoryItemMapping.getPriceWithSelectedProducts() * itemQuantity));
        }
    }

    @OnClick(R.id.incr_quantity_view)
    public void incrementQuantity()
    {
        itemQuantity++;
        quantity.setText(String.valueOf(itemQuantity));
        price.setText(String.valueOf(categoryItemMapping.getPriceWithSelectedProducts() * itemQuantity));
    }

    @OnClick(R.id.update_btn)
    public void updateItem()
    {
        cartProduct.setSelectedProducts(categoryItemMapping.getSelectedProducts());
        cartProduct.setQuantity(itemQuantity);

        presenter.updateCartProduct(cartProduct);
        intent.putExtra(Constants.Bundle.UPDATE_CATEGORY_ITEM_RESULT,Constants.Bundle.SUCCESS);

        if (getTargetFragment() != null)
        {
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK,intent);
            dismiss();
        }
    }

    private OnItemClickListener<CategoryProductMapping> onProductsGroupClickListener = new OnItemClickListener<CategoryProductMapping>()
    {
        @Override
        public void onItemClick(CategoryProductMapping item)
        {
            price.setText(String.valueOf(categoryItemMapping.getPriceWithSelectedProducts() * itemQuantity));
        }
    };

    private OnDataReceivedListener<CategoryItemMapping> onCategoryItemMappingItemReceivedListener = new OnDataReceivedListener<CategoryItemMapping>()
    {
        @Override
        public void onStartReceiving()
        {

            if (getView() != null && isAdded())
            {
                loadingView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onReceived(CategoryItemMapping data)
        {
            categoryItemMapping = data;
            categoryItem = categoryItemMapping.getCategoryItem();

            bindFragment();
            setAdapterData();


            if (getView() != null && isAdded())
            {
                loadingView.setVisibility(View.GONE);
            }
        }

        @Override
        public void onError(String error)
        {
            if (getView() != null && isAdded())
            {
                loadingView.setVisibility(View.GONE);
            }
        }
    };

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        presenter.unSubscribe();
        unbinder.unbind();
    }
}
