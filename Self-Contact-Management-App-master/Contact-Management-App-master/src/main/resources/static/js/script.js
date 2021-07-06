console.log("this is script file...");

const toggleSidebar = () => {
  if ($(".sidebar").is(":visible")) {
    //if true , then close it
    $(".sidebar").css("display", "none");
    $(".content").css("margin-left", "0%");
  } else {
    //if false , then open it
    $(".sidebar").css("display", "block");
    $(".content").css("margin-left", "20%");
  }
};

const search = () => {
  console.log("searching...");

  let query = $("#search-input").val();

  if (query == "") {
    $(".search-result").hide();
  } else {
    //search
    //console.log(query);

    //sending request to server
    let url = `http://localhost:57395/search/${query}`;

    fetch(url)
      .then((response) => {
        return response.json();
      })
      .then((data) => {
        //data..
        // console.log(data); //array
        let text = `<div class='list-group'>`;

        data.forEach((contact) => {
          text += `<a href="/contact/${contact.cId}" class='list-group-item list-group-item-action'>${contact.name}</a>`;
        });

        text += `</div>`;
        //inject html
        $(".search-result").html(text);
        //then show search box
        $(".search-result").show();
      });
  }
};

//Payment Integration
const paymentStart = () => {
  console.log("payment Integration...!!");
  let amount = $("#payment_field").val();
  console.log(amount);
  if (amount == "" || amount == null) {
    // alert("amount is required !!");
    swal("Failed!", "Amount Required... !!", "error");

    return;
  }

  //Code AJAX to create to send request to server to create order
  $.ajax({
    url: "/create_order",
    data: JSON.stringify({ amount: amount, info: "order_request" }),
    contentType: "application/json",
    type: "POST",
    dataType: "json",
    success: function (response) {
      //invoked when success
      console.log(response);
      if (response.status == "created") {
        //open payment form
        let options = {
          key: "rzp_test_GXfYoYpvSiGtCx",
          amount: response.amount,
          currency: "INR",
          name: "Smart Contact Manager",
          description: "Donation",
          image: "",
          order_id: response.id,

          handler: function (response) {
            console.log(response.razorpay_payment_id);
            console.log(response.razorpay_order_id);
            console.log(response.razorpay_signature);
            console.log("Payment successful !! ");
            //alert("Congratulations!!");
            updatePaymentOnServer(
              response.razorpay_payment_id,
              response.razorpay_order_id,
              "paid"
            );

           
          },
          prefill: {
            name: "",
            email: "",
            contact: "",
          },
          notes: {
            address: "",
          },
          theme: {
            color: "#3399cc",
          },
        };

        let rzp = new Razorpay(options);

        rzp.on("payment.failed", function (response) {
          console.log(response.error.description);
          console.log(response.error.code);
          console.log(response.error.source);
          console.log(response.error.step);
          console.log(response.error.reason);
          console.log(response.error.metadata.order_id);
          console.log(response.error.metadata.payment_id);
          // alert("Oops!! Payment failed...");
          swal("Failed!", "Oops!! Payment failed... !!", "error");
        });
        //to open forn
        rzp.open();
      }
    },
    error: function (error) {
      //invoked when error
      console.log(error);
      alert("Something Went Wrong!!");
    },
  });
};

//
function updatePaymentOnServer(payment_id, order_id, status)
{
  $.ajax({
    url: "/update_order",
    data: JSON.stringify({
      payment_id: payment_id,
      order_id: order_id,
      status: status,
    }),
    contentType: "application/json",
    type: "POST",
    dataType: "json",
    success: function (response) {
      swal(
        "Good job!",
        "Congratulations ,Payment successful !!",
        "success"
      );
    },
    error: function (error) {
      swal("Failed!", "Payment successful but cant captured on server... !!", "error");

    },
  });
}
