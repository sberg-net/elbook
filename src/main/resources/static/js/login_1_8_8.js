/*
 *  Copyright (C) 2023 sberg it-systeme GmbH
 *
 *  Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the
 *  European Commission - subsequent versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

$(window).on('load', function() {
  if (window.location.href.indexOf('?error') > -1) {
    $("#fehlerText").removeClass("d-none")
  }

  const loginModal = new bootstrap.Modal(document.getElementById("loginModal"), {backdrop: 'static', keyboard: false});
  loginModal.show();

  $("#login-submit").click(function(e) {
    const close = $("#login-close");
    const $this = $(this);

    if ($('#login-form')[0].checkValidity() === false) {
      e.preventDefault();
      e.stopPropagation();
    } else {
      $this.prepend($('<span id="loading-spinner" class="spinner-border spinner-border-sm me-3" role="status" aria-hidden="true"></span>'));
      $this.prop('disabled', true);
      close.prop('disabled', true);

      const unindexed_array = $('#login-form').serializeArray();
      const indexed_array = {};
      $.map(unindexed_array, function(n, i){
        indexed_array[n['name']] = n['value'];
      });

      // check if 2fa
      $.ajax({
        type: "POST",
        url: "mandant/is2FAenabled",
        data: JSON.stringify(indexed_array),
        contentType: "application/json",
        dataType: "json",
        success: function(data) {
          $("#loading-spinner").remove();
          $this.prop('disabled', false);
          close.prop('disabled', false);

          if(data === true) {
            $("#fehlerText").addClass("d-none")
            $("#2fa-group").show();
            $("#2fa-input").attr("required", "");
            $("#login-form").submit();
          } else {
            $("#2fa-group").hide();
            $("#2fa-input").removeAttr("required");
            $("#login-form").submit();
          }
        },
        error: function(jqXHR,textStatus,errorThrown) {
          $("#loading-spinner").remove();
          $this.prop('disabled', false);
          close.prop('disabled', false);
          const fehlerModal = new bootstrap.Modal(document.getElementById("fehlerModal"), {});
          fehlerModal.show();
        }
      });
    }

    $('#login-form').addClass('was-validated');
  });

  $("#2fa-submit").click(function() {
    $("#2fa-form").submit();
  });

  $("#login-form").submit(function(e) {
    const form = $("#login-form");
    const submit = $("#login-submit");
    const close = $("#login-close")

    if (form[0].checkValidity() === false) {
      e.preventDefault();
      e.stopPropagation();
    } else {
      submit.prop('disabled', true);
      close.prop('disabled', true);
      submit.prepend($('<span class="spinner-border spinner-border-sm me-3" role="status" aria-hidden="true"></span>'));
    }
    form.addClass('was-validated');
  });

  if (!this.matchMedia("(min-width: 992px)").matches) {
    $("nav").addClass("navbar-light");

    $(".nav-btns").removeClass("text-white");
    $(".nav-btns").addClass("text-black");

    $("#logo").removeClass("ebook-light");
  }

  $(".page-header .nav-link, .navbar-brand").on("click", function(e) {
    e.preventDefault();
    const href = $(this).attr("href");
    $("html, body").animate({
      scrollTop: $(href).offset().top - 71
    }, 600);
  });
});
