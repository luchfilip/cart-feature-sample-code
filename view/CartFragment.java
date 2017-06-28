package md.lama.rbms.consumer.rbms.views.features.cart.view;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import md.lama.rbms.consumer.rbms.R;
import md.lama.rbms.consumer.rbms.activities.MainActivity;
import md.lama.rbms.consumer.rbms.api.models.BasicRestaurantInfo;
import md.lama.rbms.consumer.rbms.listeners.OnItemClickListener;
import md.lama.rbms.consumer.rbms.views.features.cart.model.CartProduct;
import md.lama.rbms.consumer.rbms.views.features.cart.model.CartProducts;
import md.lama.rbms.consumer.rbms.presenters.adapters.CartAdapter;
import md.lama.rbms.consumer.rbms.views.features.cart.presenter.CartPresenter;
import md.lama.rbms.consumer.rbms.utils.Constants;
import md.lama.rbms.consumer.rbms.views.features.menu.MenuFragment;
import md.lama.rbms.consumer.rbms.views.features.orders.OrderTypeFragment;
import md.lama.rbms.consumer.rbms.views.restaurants.BrandsFragment;



public class CartFragment extends Fragment
{
    private CartAdapter adapter;
    private CartPresenter presenter;
    private Unbinder unbinder;
    private MainActivity activity;

    private OnCartItemListener onCartItemListener = new OnCartItemListener()
    {
        @Override
        public void OnClick(CartProduct cartProduct)
        {
            CartProducts cartProducts = presenter.getProducts();
            String currency = cartProducts != null?cartProducts.getCurrency():"";

            UpdateCartItemDialogFragment updateCartItemDialogFragment = UpdateCartItemDialogFragment.newInstance(cartProduct,currency);

            updateCartItemDialogFragment.setTargetFragment(CartFragment.this,Constants.Responses.UPDATE_MENU_CATEGORY_ITEM);
            updateCartItemDialogFragment.show(getFragmentManager(),Constants.Fragments.CART_FRAGMENT_TAG);
        }

        @Override
        public void onIncrement(String productUuid)
        {
            presenter.onIncrement(productUuid);
        }

        @Override
        public void onDecrement(String productUuid) {
            presenter.onDecrement(productUuid);
        }

        @Override
        public void onRemoveItem(final String productUuid)
        {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
            alertDialog.setTitle(getString(R.string.alert));
            alertDialog.setIcon(R.drawable.ic_warning_accent_24dp);
            alertDialog.setMessage(getString(R.string.are_you_sure_want_to_delete));
            alertDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {

                    dialogInterface.dismiss();
                }
            });
            alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {

                    presenter.onRemoveItem(productUuid);

                    adapter.setAdapterItems(presenter.getProducts());

                    dialogInterface.dismiss();
                }
            });
            alertDialog.show();
        }
    };

    private OnItemClickListener<String> onAdapterSizeChanged = new OnItemClickListener<String>()
    {
        @Override
        public void onItemClick(String item)
        {
            if (!(adapter.getItemCount()>1))
            {
                showEmptyCartView();
            }
        }
    };

    @BindView(R.id.add_product_btn) Button addProduct;
    @BindView(R.id.continue_btn) Button continueBtn;
    @BindView(R.id.products_rv) RecyclerView cartList;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.empty_cart_ll) LinearLayout emptyCartView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        presenter = new CartPresenter();
        activity = (MainActivity)getActivity();

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View layout = LayoutInflater.from(getContext()).inflate(R.layout.fragment_cart,container,false);
        unbinder = ButterKnife.bind(this, layout);

        prepareToolbar();
        prepareAdapter();


        return layout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity)getActivity()).sendScreenFragmentName(Constants.Fragments.CART_FRAGMENT_TAG);

        presenter.subscribe();
    }

    public void showEmptyCartView()
    {
        emptyCartView.setVisibility(View.VISIBLE);
        cartList.setVisibility(View.GONE);
        continueBtn.setVisibility(View.GONE);
    }

    public void prepareAdapter()
    {
        CartProducts cartProducts = presenter.getProducts();
        String currency = cartProducts != null?cartProducts.getCurrency():"";

        adapter = new CartAdapter(getContext(),currency, onAdapterSizeChanged,onCartItemListener);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);

        cartList.setLayoutManager(layoutManager);
        cartList.setAdapter(adapter);
        cartList.setHasFixedSize(true);

        if (cartProducts != null && cartProducts.getCartAmount() > 0)
        {
            adapter.setAdapterItems(cartProducts);
        }
        else
        {
            showEmptyCartView();
        }
    }

    public void prepareToolbar()
    {
        ((MainActivity)getActivity()).setToolbar(toolbar,getResources().getString(R.string.cart));
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.cart_menu,menu);
    }

    @OnClick(R.id.add_product_btn)
    public void addProducts()
    {
        String brandLabel = presenter.getBrandLabel();
        BasicRestaurantInfo restaurant = presenter.getRestaurant();
        
        if (restaurant != null && brandLabel != null)
        {
            Bundle args = new Bundle();
            args.putParcelable(Constants.Bundle.RESTAURANT_DATA_TAG,restaurant);
            args.putString(Constants.Bundle.BRAND_LABEL_TAG,brandLabel);
            args.putString(Constants.Bundle.RESTAURANT_LABEL_TAG,restaurant.getLabel());

            ((MainActivity)getActivity()).changeFragment(new MenuFragment(),args, Constants.Fragments.BRAND_LIST_FRAGMENT_TAG,true);
        }
        else
        {
            ((MainActivity)getActivity()).changeFragment(BrandsFragment.newInstance(Constants.Fragments.MENU_FRAGMENT_TAG), Constants.Fragments.BRAND_LIST_FRAGMENT_TAG,true);
        }
    }

    @OnClick(R.id.continue_btn)
    public void continueCommand()
    {
        Bundle args = new Bundle();

        String brandLabel = presenter.getBrandLabel();
        BasicRestaurantInfo restaurant = presenter.getRestaurant();

        args.putParcelable(Constants.Bundle.RESTAURANT_DATA_TAG,restaurant);
        args.putString(Constants.Bundle.BRAND_LABEL_TAG,brandLabel);

        ((MainActivity)getActivity()).changeFragment(new OrderTypeFragment(),args,Constants.Fragments.ORDER_TYPE_FRAGMENT_TAG,true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK)
        {
            switch (requestCode)
            {
                case Constants.Responses.UPDATE_MENU_CATEGORY_ITEM:
                {
                    if (data.getExtras() != null && data.getExtras().containsKey(Constants.Bundle.UPDATE_CATEGORY_ITEM_RESULT))
                    {
                        boolean result = data.getExtras().getBoolean(Constants.Bundle.UPDATE_CATEGORY_ITEM_RESULT);

                        if (result)
                        {
                            adapter.setAdapterItems(presenter.getProducts());
                        }
                    }
                    break;
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
            {
                getFragmentManager().popBackStack();
                activity.hideKeyboard();

                break;
            }
            case R.id.action_remove:
            {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                alertDialog.setTitle(getString(R.string.alert));
                alertDialog.setIcon(R.drawable.ic_warning_accent_24dp);
                alertDialog.setMessage(getString(R.string.are_you_sure_want_to_delete_all_items));
                alertDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        dialogInterface.dismiss();
                    }
                });
                alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        presenter.emptyCart();
                        adapter.emptyCart();
                        showEmptyCartView();

                        dialogInterface.dismiss();
                    }
                });


                alertDialog.show();
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        unbinder.unbind();
        presenter.unSubscribe();
    }

    public interface OnCartItemListener
    {
        void OnClick(CartProduct cartProduct);
        void onIncrement(String productUuid);
        void onDecrement(String productUuid);
        void onRemoveItem(String productUuid);
    }

}
