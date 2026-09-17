import { useEffect, useState } from "react";
import "./App.css";

function App() {
  const [products, setProducts] = useState([]);
  const [cart, setCart] = useState([]);
  const [orders, setOrders] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [notificationsOpen, setNotificationsOpen] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState("");
  const [quantity, setQuantity] = useState(1);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);

  const loadData = async () => {
    try {
      const inventoryResponse = await fetch(
        "http://localhost:8080/api/inventory"
      );
      const inventoryData = await inventoryResponse.json();
      setProducts(inventoryData);

      const ordersResponse = await fetch(
        "http://localhost:8080/api/orders"
      );
      const ordersData = await ordersResponse.json();
      setOrders(ordersData);

      const notificationResponse = await fetch(
        "http://localhost:8080/api/notifications"
      );
      const notificationData = await notificationResponse.json();
      setNotifications(notificationData.reverse());
    } catch (error) {
      console.error(error);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const addToCart = () => {
    if (!selectedProduct || Number(quantity) <= 0) return;

    const existing = cart.find(
      (item) => item.productId === selectedProduct
    );

    if (existing) {
      setCart(
        cart.map((item) =>
          item.productId === selectedProduct
            ? {
                ...item,
                quantity: item.quantity + Number(quantity),
              }
            : item
        )
      );
    } else {
      setCart([
        ...cart,
        {
          productId: selectedProduct,
          quantity: Number(quantity),
        },
      ]);
    }

    setQuantity(1);
  };

  const removeFromCart = (productId) => {
    setCart(
      cart.filter((item) => item.productId !== productId)
    );
  };

  const submitOrder = async () => {
    if (cart.length === 0) return;

    setLoading(true);
    setResult(null);

    try {
      const response = await fetch(
        "http://localhost:8080/api/orders",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            items: cart,
          }),
        }
      );

      const data = await response.json();

      setResult(data);

      if (data.status === "CONFIRMED") {
        setCart([]);
      }

      await loadData();
    } catch (error) {
      setResult({
        status: "ERROR",
        reason: "Could not connect to the server.",
      });
    } finally {
      setLoading(false);
    }
  };

  const cancelOrder = async (orderId) => {
    try {
      const response = await fetch(
        `http://localhost:8080/api/orders/${orderId}/cancel`,
        {
          method: "POST",
        }
      );

      const data = await response.json();

      setResult({
        status: response.ok ? "CANCELLED" : "ERROR",
        reason:
          typeof data === "string"
            ? data
            : "Order cancelled successfully.",
      });

      await loadData();
    } catch (error) {
      setResult({
        status: "ERROR",
        reason: "Could not cancel the order.",
      });
    }
  };

  const getProductName = (productId) => {
    const product = products.find(
      (p) => p.productId === productId
    );

    return product ? product.name : productId;
  };

  return (
    <div className="app">

      <header className="header">
  <div className="logo">
    <svg
      width="42"
      height="42"
      viewBox="0 0 48 48"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
    >
      <path
        d="M5 7H10L14 29H36L41 13H12"
        stroke="currentColor"
        strokeWidth="3.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <circle cx="18" cy="38" r="3" fill="currentColor" />
      <circle cx="34" cy="38" r="3" fill="currentColor" />
      <path
        d="M23 7L27 11L35 3"
        stroke="currentColor"
        strokeWidth="3"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  </div>

  <div className="header-actions">

    <div className="notification-wrapper">

      <button
        className="notification-button"
        onClick={() =>
          setNotificationsOpen(!notificationsOpen)
        }
        aria-label="Notifications"
      >
        <svg
          width="23"
          height="23"
          viewBox="0 0 24 24"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M18 8C18 5.79 16.21 4 14 4H10C7.79 4 6 5.79 6 8V12.5C6 13.6 5.55 14.65 4.75 15.45L3.5 16.7C3.18 17.02 3.41 17.57 3.86 17.57H20.14C20.59 17.57 20.82 17.02 20.5 16.7L19.25 15.45C18.45 14.65 18 13.6 18 12.5V8Z"
            stroke="currentColor"
            strokeWidth="1.8"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
          <path
            d="M9 20C9.6 20.7 10.55 21 12 21C13.45 21 14.4 20.7 15 20"
            stroke="currentColor"
            strokeWidth="1.8"
            strokeLinecap="round"
          />
        </svg>

        {notifications.length > 0 && (
          <span className="notification-badge">
            {notifications.length > 9
              ? "9+"
              : notifications.length}
          </span>
        )}
      </button>

      {notificationsOpen && (
        <div className="notification-dropdown">

          <div className="notification-header">
            <div>
              <h3>Notifications</h3>
              <span>
                {notifications.length} recent{" "}
                {notifications.length === 1
                  ? "notification"
                  : "notifications"}
              </span>
            </div>

            <button
              className="close-notification"
              onClick={() =>
                setNotificationsOpen(false)
              }
            >
              ×
            </button>
          </div>

          <div className="notification-list">

            {notifications.length === 0 ? (

              <div className="notification-empty">
                <div className="notification-empty-icon">
                  🔔
                </div>

                <strong>No notifications</strong>

                <span>
                  You're all caught up.
                </span>
              </div>

            ) : (

              notifications
                .slice(0, 6)
                .map((notification) => {

                  const message =
                    notification.message.toLowerCase();

                  const isLowStock =
                    message.includes("reorder");

                  const isRejected =
                    message.includes("rejected");

                  return (
                    <div
                      className="notification-row"
                      key={
                        notification.notificationId
                      }
                    >

                      <div
                        className={`notification-row-icon ${
                          isLowStock
                            ? "notification-warning"
                            : isRejected
                            ? "notification-danger"
                            : "notification-success"
                        }`}
                      >
                        {isLowStock
                          ? "!"
                          : isRejected
                          ? "×"
                          : "✓"}
                      </div>

                      <div className="notification-content">

                        <strong>
                          {isLowStock
                            ? "Low Stock Alert"
                            : isRejected
                            ? "Order Rejected"
                            : "Order Confirmed"}
                        </strong>

                        <p>
                          {notification.message}
                        </p>

                        <span>
                          Just now
                        </span>

                      </div>

                    </div>
                  );
                })
            )}

          </div>

          <div className="notification-footer">
            <button
              onClick={() => {
                setNotificationsOpen(false);

                window.scrollTo({
                  top:
                    document.body.scrollHeight,
                  behavior: "smooth",
                });
              }}
            >
              View activity feed
            </button>
          </div>

        </div>
      )}
    </div>

    <div className="status-badge">
      <span className="status-dot"></span>
      System Online
    </div>

  </div>
</header>

      <main className="container">

        {/* INVENTORY */}
        <section className="card">

          <div className="section-header">
            <div>
              <h2>Inventory</h2>
              <p>Live product availability</p>
            </div>

            <button
              className="refresh-button"
              onClick={loadData}
            >
              ↻ Refresh
            </button>
          </div>

          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Product ID</th>
                  <th>Product</th>
                  <th>Stock</th>
                  <th>Status</th>
                </tr>
              </thead>

              <tbody>
                {products.map((product) => {
                  const lowStock = product.stock < 5;
                  const outOfStock = product.stock === 0;

                  return (
                    <tr
                      key={product.productId}
                      className={
                        lowStock ? "low-stock-row" : ""
                      }
                    >
                      <td>
                        <span className="product-id">
                          {product.productId}
                        </span>
                      </td>

                      <td className="product-name">
                        {product.name}
                      </td>

                      <td className="stock-number">
                        {product.stock}
                      </td>

                      <td>
                        {outOfStock ? (
                          <span className="badge badge-danger">
                            Out of Stock
                          </span>
                        ) : lowStock ? (
                          <span className="badge badge-warning">
                            Low Stock
                          </span>
                        ) : (
                          <span className="badge badge-success">
                            Available
                          </span>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

        </section>

        <div className="two-column">

          {/* CART */}
          <section className="card">

            <div className="section-header">
              <div>
                <h2>New Order</h2>
                <p>Add products to your cart</p>
              </div>
            </div>

            <div className="form-group">
              <label>Product</label>

              <select
                value={selectedProduct}
                onChange={(e) =>
                  setSelectedProduct(e.target.value)
                }
              >
                <option value="">
                  Select a product
                </option>

                {products.map((product) => (
                  <option
                    key={product.productId}
                    value={product.productId}
                  >
                    {product.productId} — {product.name}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label>Quantity</label>

              <input
                type="number"
                min="1"
                value={quantity}
                onChange={(e) =>
                  setQuantity(e.target.value)
                }
              />
            </div>

            <button
              className="primary-button"
              onClick={addToCart}
            >
              + Add to Cart
            </button>

            <div className="cart-section">

              <div className="cart-title">
                <h3>Cart</h3>
                <span className="cart-count">
                  {cart.length}
                </span>
              </div>

              {cart.length === 0 ? (
                <div className="empty-state">
                  <div className="empty-icon">🛒</div>
                  <p>Your cart is empty</p>
                  <span>
                    Add products above to create an order.
                  </span>
                </div>
              ) : (
                <>
                  <div className="cart-items">

                    {cart.map((item) => (
                      <div
                        className="cart-item"
                        key={item.productId}
                      >
                        <div>
                          <strong>
                            {item.productId}
                          </strong>

                          <p>
                            {getProductName(
                              item.productId
                            )}
                          </p>
                        </div>

                        <div className="cart-right">
                          <span>
                            × {item.quantity}
                          </span>

                          <button
                            className="remove-button"
                            onClick={() =>
                              removeFromCart(
                                item.productId
                              )
                            }
                          >
                            Remove
                          </button>
                        </div>
                      </div>
                    ))}

                  </div>

                  <button
                    className="submit-button"
                    onClick={submitOrder}
                    disabled={loading}
                  >
                    {loading
                      ? "Processing..."
                      : "Submit Order"}
                  </button>
                </>
              )}
            </div>
          </section>

          {/* RESULT */}
          <section className="card">

            <div className="section-header">
              <div>
                <h2>Latest Result</h2>
                <p>Most recent order activity</p>
              </div>
            </div>

            {!result ? (
              <div className="empty-state result-empty">
                <div className="empty-icon">📋</div>
                <p>No recent order</p>
                <span>
                  Your order result will appear here.
                </span>
              </div>
            ) : (
              <div
                className={`result-box ${
                  result.status === "CONFIRMED"
                    ? "result-success"
                    : result.status === "REJECTED"
                    ? "result-rejected"
                    : "result-other"
                }`}
              >
                <div className="result-status">
                  <span>
                    {result.status === "CONFIRMED"
                      ? "✓"
                      : result.status === "REJECTED"
                      ? "!"
                      : "i"}
                  </span>

                  <strong>{result.status}</strong>
                </div>

                <p>{result.reason}</p>

                {result.items &&
                  result.items.map((item) => (
                    <div
                      className="result-item"
                      key={item.productId}
                    >
                      <span>
                        {item.productId}
                      </span>

                      <span>
                        {item.outcome}
                      </span>
                    </div>
                  ))}

                {result.inventory && (
                  <div className="remaining-stock">
                    Remaining stock:{" "}
                    <strong>
                      {result.inventory.stock}
                    </strong>
                  </div>
                )}
              </div>
            )}

          </section>

        </div>

        {/* ORDER HISTORY */}
        <section className="card">

          <div className="section-header">
            <div>
              <h2>Order History</h2>
              <p>View and manage previous orders</p>
            </div>

            <span className="history-count">
              {orders.length} orders
            </span>
          </div>

          {orders.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">📦</div>
              <p>No orders yet</p>
              <span>
                Completed orders will appear here.
              </span>
            </div>
          ) : (
            <div className="orders-list">

              {orders
                .slice()
                .reverse()
                .map((order) => (

                  <div
                    className="order-item"
                    key={order.orderId}
                  >

                    <div className="order-info">

                      <div className="order-number">
                        Order O{order.orderId}
                      </div>

                      <div className="order-reason">
                        {order.reason}
                      </div>

                    </div>

                    <div className="order-actions">

                      <span
                        className={`badge ${
                          order.status === "CONFIRMED"
                            ? "badge-success"
                            : order.status === "CANCELLED"
                            ? "badge-neutral"
                            : "badge-danger"
                        }`}
                      >
                        {order.status}
                      </span>

                      {order.status === "CONFIRMED" && (
                        <button
                          className="cancel-button"
                          onClick={() =>
                            cancelOrder(
                              order.orderId
                            )
                          }
                        >
                          Cancel
                        </button>
                      )}

                    </div>

                  </div>

                ))}

            </div>
          )}

        </section>

        {/* NOTIFICATIONS */}
        <section className="card">

          <div className="section-header">
            <div>
              <h2>Activity Feed</h2>
              <p>System notifications and alerts</p>
            </div>

            <span className="notification-count">
              {notifications.length}
            </span>
          </div>

          {notifications.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">🔔</div>
              <p>No notifications</p>
              <span>
                Order and inventory events will appear here.
              </span>
            </div>
          ) : (
            <div className="notifications-list">

              {notifications.map((notification) => (

                <div
                  className="notification-item"
                  key={notification.notificationId}
                >
                  <div className="notification-icon">
                    🔔
                  </div>

                  <div>
                    <strong>
                      {notification.message}
                    </strong>

                    <p>
                      System activity recorded
                    </p>
                  </div>
                </div>

              ))}

            </div>
          )}

        </section>

      </main>

      <footer>
        Shop Order System • React + Spring Boot + Supabase
      </footer>

    </div>
  );
}

export default App;