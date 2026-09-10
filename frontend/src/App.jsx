import { useState } from "react";

function App() {
  const products = [
    { id: "P100", name: "Wireless Mouse" },
    { id: "P200", name: "Mechanical Keyboard" },
    { id: "P300", name: "USB-C Hub" }
  ];

  const [productId, setProductId] = useState("P100");
  const [quantity, setQuantity] = useState(1);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);

  const placeOrder = async () => {
    setLoading(true);
    setResult(null);

    try {
      const response = await fetch("http://localhost:8080/api/orders", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          productId: productId,
          quantity: Number(quantity)
        })
      });

      const data = await response.json();
      setResult(data);
    } catch (error) {
      setResult({
        status: "ERROR",
        reason: "Could not connect to the server."
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        maxWidth: "600px",
        margin: "50px auto",
        padding: "30px",
        fontFamily: "Arial"
      }}
    >
      <h1>Shop Order System</h1>

      <label>
        <strong>Product</strong>
      </label>

      <br />

      <select
        value={productId}
        onChange={(e) => setProductId(e.target.value)}
        style={{
          width: "100%",
          padding: "10px",
          marginTop: "5px"
        }}
      >
        {products.map((product) => (
          <option key={product.id} value={product.id}>
            {product.id} - {product.name}
          </option>
        ))}
      </select>

      <br />
      <br />

      <label>
        <strong>Quantity</strong>
      </label>

      <br />

      <input
        type="number"
        min="1"
        value={quantity}
        onChange={(e) => setQuantity(e.target.value)}
        style={{
          width: "100%",
          padding: "10px",
          marginTop: "5px",
          boxSizing: "border-box"
        }}
      />

      <br />
      <br />

      <button
        onClick={placeOrder}
        disabled={loading}
        style={{
          padding: "10px 20px",
          cursor: "pointer"
        }}
      >
        {loading ? "Processing..." : "Submit Order"}
      </button>

      {result && (
        <div
          style={{
            marginTop: "30px",
            padding: "20px",
            border: "1px solid #ccc"
          }}
        >
          <h2>Order Result</h2>

          <p>
            <strong>Status:</strong> {result.status}
          </p>

          <p>
            <strong>Reason:</strong> {result.reason}
          </p>

          {result.inventory && (
            <>
              <p>
                <strong>Product:</strong> {result.inventory.name}
              </p>

              <p>
                <strong>Remaining Stock:</strong>{" "}
                {result.inventory.stock}
              </p>
            </>
          )}
        </div>
      )}
    </div>
  );
}

export default App;