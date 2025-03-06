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

(function($) {
	$.fn.serializeFormJSON = function() {

	var o = {};
	var a = this.serializeArray();
	$.each(a, function() {
		if (o[this.name]) {
			if (!o[this.name].push) {
				o[this.name] = [o[this.name]];
			}
			o[this.name].push(this.value || '');
		} else {
			o[this.name] = this.value || '';
		}
	});
	return o;
};
})(jQuery);

(function($) {
  $.fn.serializeFiles = function() {
    var obj = $(this);
    /* ADD FILE TO PARAM AJAX */
    var formData = new FormData();
    $.each($(obj).find("input[type='file']"), function(i, tag) {
      $.each($(tag)[0].files, function(i, file) {
        formData.append(tag.name, file);
      });
    });
    var params = $(obj).serializeArray();
    $.each(params, function (i, val) {
      formData.append(val.name, val.value);
    });
    return formData;
  };
})(jQuery);

$(document).keyup(function(event) {
  if ($("#vzdSearchForm input").is(":focus") && event.key == "Enter") {
    vzdEintragUebersicht();
  }
});

function vzdColClickHandle(el) {
  $("#vzdOverlay").attr("style", "display:block");
  setTimeout(function() {
    var id = $(el).attr('id');
    var checked = $(el).is(":checked");
    elbookContext.vzdColVisible[id] = checked;
    if (checked) {
      $('.'+id).show();
    }
    else {
      $('.'+id).hide();
    }
    $("#vzdOverlay").attr("style", "display:none");
  }, 0);
}

function togglePwdVisibility(id) {
  let x = document.getElementById(id);
  if (x.type === "password") {
    x.type = "text";
  } else {
    x.type = "password";
  }
}

const elbookContext = {
  toDeleteMandant: null,
  toDeleteReport: null,
  toDeleteVzdEintrag: null,
  toDeleteTsp: null,
  toDeleteConnectionId: null,
  toDeleteConnectionMutex: null,
  toDeleteKartendatentransfer: null,
  toDeleteKartendatentransferPerson: null,
  toSendKartendatentransfer: null,
  searchValue: '',
  connectionSearchValue: '',
  logentrySearchValue: '',
  checkSearchData: {
    'uid': true,
    'telematikId': true,
    'domainId': true,
    'telematikIdSubstr': true,
    'displayName': true,
    'cn': true,
    'givenName': true,
    'sn': true,
    'otherName': true,
    'title': true,
    'organization': true,
    'streetAddress': true,
    'postalCode': true,
    'localityName': true,
    'countryCode': true,
    'specialization': true,
    'professionOid': true,
    'meta': true,
    'holder': true,
    'changeDateTimeFrom': true,
    'changeDateTimeTo': true,
    'maxKomLeAdr': true,
    'mail': true,
    'komLeData': true,
    'kimData': true
  },
  arraySearchData: {
    'domainId': true,
    'specialization': true,
    'professionOid': true,
    'meta': true,
    'holder': true
  },
  searchData: {
    readDirSyncEntryCommand: {
      uid: '',
      telematikId: '',
      domainId: [],
      telematikIdSubstr: '',

      displayName: '',
      cn: '',
      givenName: '',
      sn: '',
      otherName: '',
      title: '',
      organization: '',

      streetAddress: '',
      postalCode: '',
      localityName: '',
      countryCode: '',
      stateOrProvinceName: 'UNKNOWN',

      specialization: [],
      professionOid: [],
      entryType: 'UNKNOWN',

      dataFromAuthority: 'UNDEFINED',
      personalEntry: 'UNDEFINED',
      active: 'UNDEFINED',
      meta: [],
      holder: [],
      changeDateTimeFrom: '',
      changeDateTimeTo: '',
      maxKomLeAdr: ''
    },
    readDirSyncEntryFaAttributesCommand: {
      mail: '',
      komLeData: '',
      kimData: ''
    }
  },
  vzdColVisible: {
    vzdentry_col_id: false,
    vzdentry_col_tid: true,
    vzdentry_col_domainid: false,
    vzdentry_col_displayname: true,
    vzdentry_col_cn: true,
    vzdentry_col_othername: false,
    vzdentry_col_organization: false,
    vzdentry_col_givenname: true,
    vzdentry_col_sn: true,
    vzdentry_col_title: false,
    vzdentry_col_streetaddress: false,
    vzdentry_col_postalcode: true,
    vzdentry_col_localityname: true,
    vzdentry_col_stateorprovincename: false,
    vzdentry_col_countrycode: false,
    vzdentry_col_specialization: false,
    vzdentry_col_professionoid: false,
    vzdentry_col_entrytype: false,
    vzdentry_col_holder: false,
    vzdentry_col_active: false,
    vzdentry_col_meta: false,
    vzdentry_col_maxkomleadr: false,
    vzdentry_col_datafromauthority: false,
    vzdentry_col_personalentry: false,
    vzdentry_col_changedatetime: false
  },
  telematikId: '',
  baseEntryUid: '',
  baseEntryActive: null,
  certUid: '',
  certCn: '',
  tspantragexportDataLimit: ''
}

function toggleDeacivate(id, elemArr) {
  const checked = $("#"+id).is(':checked');
  elemArr.forEach(elemId => {
    $('#'+elemId).prop("disabled", checked);
  });
}

function vzdEintragUebersicht(showCache,makeCSVExport) {

  $("#elbook-spinner").attr("style", "");

  if ($("#vzdEintragFormRow").length > 0) {
    $("#vzdEintragFormRow").remove();
  }
  if ($("#vzdEintragZertRow").length > 0) {
    $("#vzdEintragZertRow").remove();
  }

  if (showCache) {
    $("#elbook-spinner").attr("style", "display:none");
    return;
  }

  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  makeCSVExport = !makeCSVExport?false:true;
  var initalLoading = true;
  var searchAttrContainerVis = {};

  if ($('#vzdSearchForm #uid').length > 0) {
    initalLoading = false;
    Object.keys(elbookContext.searchData.readDirSyncEntryCommand).forEach(function(key,index) {
      if (elbookContext.arraySearchData[key]) {
        elbookContext.searchData.readDirSyncEntryCommand[key] = $('#' + key).val().trim() !== ''?$('#' + key).val().split(','):[];
      }
      else {
        elbookContext.searchData.readDirSyncEntryCommand[key] = $('#' + key).val().trim();
      }

      if (elbookContext.searchData.readDirSyncEntryCommand[key] === '') {
        elbookContext.searchData.readDirSyncEntryCommand[key] = null;
      }

    });

    Object.keys(elbookContext.searchData.readDirSyncEntryFaAttributesCommand).forEach(function(key,index) {
      if ($('#' + key).val()) {
        elbookContext.searchData.readDirSyncEntryFaAttributesCommand[key] = $('#' + key).val().trim();
      }
      if (elbookContext.searchData.readDirSyncEntryFaAttributesCommand[key] === '') {
        elbookContext.searchData.readDirSyncEntryFaAttributesCommand[key] = null;
      }

    });

    $('#vzdEintragTableError').empty();
    $('#vzdEintragTableError').attr("style", "display:none");
    var validSearch = false;

    Object.keys(elbookContext.searchData.readDirSyncEntryCommand).forEach(function(key,index) {
      if (elbookContext.checkSearchData[key]) {
        if (Array.isArray(elbookContext.searchData.readDirSyncEntryCommand[key]) && elbookContext.searchData.readDirSyncEntryCommand[key].length > 0) {
          validSearch = true;
        }
        else if (!Array.isArray(elbookContext.searchData.readDirSyncEntryCommand[key]) && elbookContext.searchData.readDirSyncEntryCommand[key] !== null) {
          validSearch = true;
        }
      }
    });

    if (!validSearch) {
      Object.keys(elbookContext.searchData.readDirSyncEntryFaAttributesCommand).forEach(function(key,index) {
        if (elbookContext.checkSearchData[key]) {
          if (Array.isArray(elbookContext.searchData.readDirSyncEntryFaAttributesCommand[key]) && elbookContext.searchData.readDirSyncEntryFaAttributesCommand[key].length > 0) {
            validSearch = true;
          }
          else if (!Array.isArray(elbookContext.searchData.readDirSyncEntryFaAttributesCommand[key]) && elbookContext.searchData.readDirSyncEntryFaAttributesCommand[key] !== null) {
            validSearch = true;
          }
        }
      });
    }

    if (!validSearch &&
      (
        elbookContext.searchData.readDirSyncEntryCommand['stateOrProvinceName'] !== 'UNKNOWN' || elbookContext.searchData.readDirSyncEntryCommand['entryType'] !== 'UNKNOWN' ||
        elbookContext.searchData.readDirSyncEntryCommand['dataFromAuthority'] !== 'UNDEFINED' || elbookContext.searchData.readDirSyncEntryCommand['personalEntry'] !== 'UNDEFINED' ||
        elbookContext.searchData.readDirSyncEntryCommand['active'] !== 'UNDEFINED'
      )) {
      validSearch = true;
    }

    if (!validSearch) {
      $('#vzdEintragTableError').attr("style", "");
      $('#vzdEintragTableError').text("Bitte mindestens einen Suchparameter auswählen");
      $("#elbook-spinner").attr("style", "display:none");
      return;
    }

    $(".searchAttrContainer").each(index => {
      searchAttrContainerVis[$($(".searchAttrContainer")[index]).attr("id")] = $($(".searchAttrContainer")[index]).is(':visible');
    });

  }

  $("#vzdTableContainer").attr("style", "");
  $("#vzdTableContainer").empty();
  $.ajax({
    type: "POST",
    url: $("#vzdTableContainer").attr("action")+'verzeichnisdienst/uebersicht/'+makeCSVExport+'/'+initalLoading,
    data: JSON.stringify(elbookContext.searchData),
    contentType: "application/json",
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#vzdTableContainer").append(data);

      Object.keys(elbookContext.searchData.readDirSyncEntryCommand).forEach(function(key,index) {
        if (elbookContext.arraySearchData[key]) {
          if (elbookContext.searchData.readDirSyncEntryCommand[key] && elbookContext.searchData.readDirSyncEntryCommand[key].length > 0) {
            $('#' + key).val(elbookContext.searchData.readDirSyncEntryCommand[key].join(','));
          }
        }
        else {
          $('#'+key).val(elbookContext.searchData.readDirSyncEntryCommand[key]);
        }
      });

      Object.keys(elbookContext.searchData.readDirSyncEntryFaAttributesCommand).forEach(function(key,index) {
        $('#'+key).val(elbookContext.searchData.readDirSyncEntryFaAttributesCommand[key]);
      });

      $('#vzdColVisibleContainer input').each(function() {
        var id = $(this).attr('id');
        if (elbookContext.vzdColVisible[id]) {
          $('.'+id).show();
          $('#'+id).prop( "checked", true );
        }
        else {
          $('.'+id).hide();
          $('#'+id).prop( "checked", false );
        }
      });

      if (makeCSVExport && $('#csvExportErstellen').length > 0) {
        window.open($('#csvExportErstellen').attr("href"), '_blank');
        $('#csvExportErstellen').remove();
      }

      Object.keys(searchAttrContainerVis).forEach(function(key,index) {
        if (searchAttrContainerVis[key]) {
          $('#'+key).show();
        }
        else {
          $('#'+key).hide();
        }
      });
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#vzdTableContainer").append(jqXHR.responseText);
    }
  });
}

function vzdEintragLoeschen() {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  const confirmDialog4DeleteVzdEintragModal = bootstrap.Modal.getInstance(document.getElementById('confirmDialog4DeleteVzdEintrag'));

  $("#elbook-spinner").attr("style", "");
  $.ajax({
    type: "DELETE",
    url: $("#vzdTableContainer").attr("action")+"verzeichnisdienst/loeschen/"+elbookContext.toDeleteVzdEintrag+"/"+btoa(elbookContext.telematikId),
    contentType: "application/json",
    dataType: "json",
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      confirmDialog4DeleteVzdEintragModal.hide();
      setTimeout(vzdEintragUebersicht,0);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#vzdEintragTableError").attr("style", "");
      $("#vzdEintragTableError").append("Beim Löschen des Eintrages ist ein Fehler aufgetreten.");
      confirmDialog4DeleteVzdEintragModal.hide();
    }
  });
}

function vzdEintragAuthSpeichern() {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  const res = document.getElementById("vzdAuthForm").checkValidity();
  $('.needs-validation').addClass('was-validated');
  if (!res) {
    return;
  }

  $("#elbook-spinner").attr("style", "");
  const data = $("#vzdAuthForm").serializeFormJSON();
  $.ajax({
    type: "POST",
    url: $("#vzdTableContainer").attr("action")+"verzeichnisdienst/authspeichern/"+$('#speichereDauerhaft').prop( "checked" ),
    data: JSON.stringify(data),
    contentType: "application/json",
    dataType: "json",
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      setTimeout(vzdEintragUebersicht, 0);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#vzdAuthFormError").attr("style", "");
      $("#vzdAuthFormError").empty();
      $("#vzdAuthFormError").append("Beim Speichern der Authentifizierungsdaten ist ein Fehler aufgetreten.");
    }
  });
}

function vzdEintragAuthLoeschen() {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  const confirmDialog4DeleteVzdAuthModal = bootstrap.Modal.getInstance(document.getElementById('confirmDialog4DeleteVzdAuth'));

  $("#elbook-spinner").attr("style", "");
  $.ajax({
    type: "DELETE",
    url: $("#vzdTableContainer").attr("action")+"verzeichnisdienst/authloeschen",
    contentType: "application/json",
    dataType: "json",
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      confirmDialog4DeleteVzdAuthModal.hide();
      $('#vzdSearchForm').remove();
      setTimeout(vzdEintragUebersicht, 0);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#vzdEintragTableError").attr("style", "");
      $("#vzdEintragTableError").append("Beim Löschen der Authentifizierungsdaten ist ein Fehler aufgetreten.");
      confirmDialog4DeleteVzdAuthModal.hide();
    }
  });
}

function vzdEintragSpeichern() {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  const res = document.getElementById("vzdEintragForm").checkValidity();
  $('.needs-validation').addClass('was-validated');
  if (!res) {
    return;
  }

  $("#elbook-spinner").attr("style", "");
  const data = $("#vzdEintragForm").serializeFormJSON();
  data.specialization = data.specialization.trim() !== ''?data.specialization.split(','):[];
  data.domainId = data.domainId.trim() !== ''?data.domainId.split(','):[];
  if (!data.telematikId) {
    data.telematikId = $('#vzdEintragForm input[name=telematikId]').val();
  }

  $.ajax({
    type: "POST",
    url: $("#vzdTableContainer").attr("action")+"verzeichnisdienst/speichern",
    data: JSON.stringify(data),
    contentType: "application/json",
    dataType: "json",
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      setTimeout(vzdEintragUebersicht(false), 0);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#vzdEintragFormError").attr("style", "");
      $("#vzdEintragFormError").empty();
      $("#vzdEintragFormError").append("Beim Speichern ist ein Fehler aufgetreten");
    }
  });
}

function vzdEintragStatusAendern() {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  const confirmDialog4BaseEntryActivateModal = bootstrap.Modal.getInstance(document.getElementById('confirmDialog4BaseEntryActivate'));
  const confirmDialog4BaseEntryDeactivateModal = bootstrap.Modal.getInstance(document.getElementById('confirmDialog4BaseEntryDeactivate'));

  $("#elbook-spinner").attr("style", "");
  const command = {};
  command.telematikId = elbookContext.telematikId;
  command.uid = elbookContext.baseEntryUid;
  command.active = elbookContext.baseEntryActive;

  $.ajax({
    type: "POST",
    url: $("#vzdTableContainer").attr("action")+"verzeichnisdienst/statusaendern",
    data: JSON.stringify(command),
    contentType: "application/json",
    dataType: "json",
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function() {
      $("#elbook-spinner").attr("style", "display:none");
      if (command.active) {
        confirmDialog4BaseEntryActivateModal.hide();
      }
      else {
        confirmDialog4BaseEntryDeactivateModal.hide();
      }
      setTimeout(vzdEintragUebersicht(false), 0);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      if (command.active) {
        confirmDialog4BaseEntryActivateModal.hide();
      }
      else {
        confirmDialog4BaseEntryDeactivateModal.hide();
      }
      $("#vzdEintragFormError").attr("style", "");
      $("#vzdEintragFormError").empty();
      $("#vzdEintragFormError").append("Beim Ändern des Status ist ein Fehler aufgetreten");
    }
  });
}

function vzdZertifikatEintragSpeichern(uid,telematikId,zertIdx) {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  $("#elbook-spinner").attr("style", "");
  const certContent = $('div[data-idx='+zertIdx+']').find('textarea')[0].value;

  $.ajax({
    type: "POST",
    url: $("#vzdTableContainer").attr("action")+"verzeichnisdienst/zertifikate/speichern/" + uid + "/" + btoa(telematikId),
    data: JSON.stringify(certContent),
    contentType: "application/json",
    dataType: "json",
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      setTimeout(() => { vzdEintragZertifikateLaden(uid, telematikId, false, 0) }, 0);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#vzdZertifikatEintragFormError").attr("style", "");
      $("#vzdZertifikatEintragFormError").empty();
      $("#vzdZertifikatEintragFormError").append(JSON.parse(jqXHR.responseText).message);
    }
  });
}

function vzdZertifikatEintragLoeschen() {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  const confirmDialog4DeleteVzdEintragZertModal = bootstrap.Modal.getInstance(document.getElementById('confirmDialog4DeleteVzdEintragZert'));

  $("#elbook-spinner").attr("style", "");
  $.ajax({
    type: "DELETE",
    url: $("#vzdTableContainer").attr("action")+"verzeichnisdienst/zertifikate/loeschen/"+elbookContext.certUid+"/"+elbookContext.certCn+"/"+btoa(elbookContext.telematikId),
    contentType: "application/json",
    dataType: "json",
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      confirmDialog4DeleteVzdEintragZertModal.hide();
      setTimeout(() => { vzdEintragZertifikateLaden(elbookContext.certUid, elbookContext.telematikId, false, 0) }, 0);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#vzdZertifikatEintragFormError").attr("style", "");
      $("#vzdZertifikatEintragFormError").append("Beim Löschen des Zertifikats ist ein Fehler aufgetreten.");
      confirmDialog4DeleteVzdEintragZertModal.hide();
    }
  });
}

function vzdEintragZertifikateLaden(uid,telematikId,addNewZert,zertSize) {
  $("#vzdOverlay").attr("style", "display:block");

  if ($("#vzdEintragFormRow").length > 0) {
    $("#vzdEintragFormRow").remove();
  }
  if ($("#vzdEintragZertRow").length > 0) {
    $("#vzdEintragZertRow").remove();
  }

  $.ajax({
    type: "GET",
    url: $("#vzdTableContainer").attr("action")+'verzeichnisdienst/zertifikate/lade/' + uid + '/' + telematikId+ '/' + addNewZert + '/' + zertSize,
    success: function(data) {
      $("#vzdOverlay").attr("style", "display:none");
      $("#"+uid).after(data);
      $('.needs-validation').addClass('was-validated');
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#vzdOverlay").attr("style", "display:none");
      $("#"+uid).after(jqXHR.responseText);
    }
  });
}

function vzdEintragLaden(uid) {
  $("#vzdOverlay").attr("style", "display:block");
  if ($("#vzdEintragFormRow").length > 0) {
    $("#vzdEintragFormRow").remove();
  }
  if ($("#vzdEintragZertRow").length > 0) {
    $("#vzdEintragZertRow").remove();
  }
  $.ajax({
    type: "GET",
    url: $("#vzdTableContainer").attr("action")+'verzeichnisdienst/lade/' + uid,
    success: function(data) {
      $("#vzdOverlay").attr("style", "display:none");
      if (uid === -1) {
        $("#vzdEintragTableBody").prepend(data);
      }
      else {
        $("#"+uid).after(data);
      }
      const editable =  $("#vzdEintragForm #bearbeitenEintraege").val();
      if (editable === 'true') {
        document.getElementById("vzdEintragForm").checkValidity();
        $('.needs-validation').addClass('was-validated');
      }
      else {
        $("#vzdEintragForm input").prop("disabled", true);
        $("#vzdEintragForm select").prop("disabled", true);
        $("#vzdEintragForm textarea").prop("disabled", true);
      }
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#vzdOverlay").attr("style", "display:none");
      if (uid === -1) {
        $("#vzdEintragTableBody").prepend(jqXHR.responseText);
      }
      else {
        $("#"+uid).after(data);
      }
    }
  });
}

function mandantLoeschen() {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  const confirmDialog4DeleteMandantModal = bootstrap.Modal.getInstance(document.getElementById('confirmDialog4DeleteMandant'));

  $("#elbook-spinner").attr("style", "");

  $.ajax({
    type: "DELETE",
    url: $("#mandantTableContainer").attr("action")+"mandant/loeschen/"+elbookContext.toDeleteMandant,
    contentType: "application/json",
    dataType: "json",
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      confirmDialog4DeleteMandantModal.show();
      setTimeout(mandantUebersicht,0);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#mandantTableError").attr("style", "");
      $("#mandantTableError").append(JSON.parse(jqXHR.responseText).message);
      confirmDialog4DeleteMandantModal.show();
    }
  });
}

function mandantSpeichern() {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  const res = document.getElementById("mandantForm").checkValidity();
  $('.needs-validation').addClass('was-validated');
  if (!res) {
    return;
  }

  $("#elbook-spinner").attr("style", "");

  const data = $("#mandantForm").serializeFormJSON();
  data.bearbeitenEintraege = $("#mandantForm #bearbeitenEintraege").is(":checked");
  data.filternEintraege = $("#mandantForm #filternEintraege").is(":checked");
  data.bearbeitenNurFiltereintraege = $("#mandantForm #bearbeitenNurFiltereintraege").is(":checked");

  if ($("#mandantForm #goldLizenz").length > 0) {
    data.goldLizenz = $("#mandantForm #goldLizenz").is(":checked");
  }
  else {
    data.goldLizenz = false;
  }

  $.ajax({
    type: "POST",
    url: $("#mandantFormContainer").attr("action")+"mandant/speichern",
    data: JSON.stringify(data),
    contentType: "application/json",
    dataType: "json",
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      setTimeout(mandantUebersicht, 0);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#mandantFormError").attr("style", "");
      $("#mandantFormError").empty();
      $("#mandantFormError").append(JSON.parse(jqXHR.responseText).message);
    }
  });
}

function mandantLaden(userId) {
  $("#elbook-spinner").attr("style", "");
  $("#mandantPwdResetContainer").attr("style", "display:none");
  $("#mandantTableContainer").attr("style", "display:none");
  $("#mandantFormContainer").attr("style", "");
  $("#mandantFormContainer").empty();
  $.ajax({
    type: "GET",
    url: $("#mandantFormContainer").attr("action")+'mandant/lade/' + userId,
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#mandantFormContainer").append(data);
      document.getElementById("mandantForm").checkValidity();
      $('.needs-validation').addClass('was-validated');
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#mandantFormContainer").append(jqXHR.responseText);
    }
  });
}

function mandantUebersicht(searchValue) {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  if (searchValue === null || searchValue === undefined) {
    searchValue = elbookContext.searchValue;
  }
  elbookContext.searchValue = searchValue?searchValue:'';

  $("#elbook-spinner").attr("style", "");
  $("#mandantFormContainer").attr("style", "display:none");
  $("#mandantTableContainer").attr("style", "");
  $("#mandantTableContainer").empty();
  $.ajax({
    type: "POST",
    url: $("#mandantTableContainer").attr("action")+'mandant/uebersicht',
    data: 'searchValue='+(searchValue?searchValue:''),
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#mandantTableContainer").append(data);
      $("#search").val(elbookContext.searchValue);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#mandantTableContainer").append(jqXHR.responseText);
    }
  });
}

function glossarUebersicht(searchValue, searchType) {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  if (searchValue === null || searchValue === undefined) {
    searchValue = elbookContext.searchValue;
  }
  elbookContext.searchValue = searchValue?searchValue:'';
  searchType=searchType?searchType:'';

  $("#elbook-spinner").attr("style", "");
  $("#glossarTableContainer").attr("style", "");
  $("#glossarTableContainer").empty();
  $.ajax({
    type: "POST",
    url: $("#glossarTableContainer").attr("action")+'glossar/uebersicht',
    data: 'searchValue='+(searchValue?searchValue:'')+'&searchType='+searchType,
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#glossarTableContainer").append(data);
      if (searchType === 'telematikId') {
        $("#telematikIdSearch").val(elbookContext.searchValue);
      }
      else if (searchType === 'professionOID') {
        $("#professionOIDSearch").val(elbookContext.searchValue);
      }
      else if (searchType === 'holder') {
        $("#holderSearch").val(elbookContext.searchValue);
      }
      else if (searchType === 'specialization') {
        $("#specializationSearch").val(elbookContext.searchValue);
      }
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#glossarTableContainer").append(jqXHR.responseText);
    }
  });
}

function reportUebersicht() {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  $("#elbook-spinner").attr("style", "");
  $("#reportFormContainer").attr("style", "display:none");
  $("#reportTableContainer").attr("style", "");
  $("#reportTableContainer").empty();
  $.ajax({
    type: "POST",
    url: $("#reportTableContainer").attr("action")+'report/uebersicht',
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#reportTableContainer").append(data);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#reportTableContainer").append(jqXHR.responseText);
    }
  });
}

function reportLaden(reportId) {
  $("#elbook-spinner").attr("style", "");
  $("#reportTableContainer").attr("style", "display:none");
  $("#reportFormContainer").attr("style", "");
  $("#reportFormContainer").empty();
  $.ajax({
    type: "GET",
    url: $("#reportFormContainer").attr("action")+'report/lade/' + reportId,
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#reportFormContainer").append(data);
      document.getElementById("reportForm").checkValidity();
      $('.needs-validation').addClass('was-validated');
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#reportFormContainer").append(jqXHR.responseText);
    }
  });
}

function reportSpeichern() {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  const res = document.getElementById("reportForm").checkValidity();
  $('.needs-validation').addClass('was-validated');
  if (!res) {
    return;
  }

  $("#elbook-spinner").attr("style", "");

  const data = $("#reportForm").serializeFormJSON();

  $.ajax({
    type: "POST",
    url: $("#reportFormContainer").attr("action")+"report/speichern",
    data: JSON.stringify(data),
    contentType: "application/json",
    dataType: "json",
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      setTimeout(reportUebersicht, 0);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#reportFormError").attr("style", "");
      $("#reportFormError").empty();
      $("#reportFormError").append(JSON.parse(jqXHR.responseText).message);
    }
  });
}

function reportLoeschen() {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  const confirmDialog4DeleteReportModal = bootstrap.Modal.getInstance(document.getElementById('confirmDialog4DeleteReport'));

  $("#elbook-spinner").attr("style", "");

  $.ajax({
    type: "DELETE",
    url: $("#reportTableContainer").attr("action")+"report/loeschen/"+elbookContext.toDeleteReport,
    contentType: "application/json",
    dataType: "json",
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      confirmDialog4DeleteReportModal.hide();
      setTimeout(reportUebersicht,0);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#reportTableError").attr("style", "");
      $("#reportTableError").append(JSON.parse(jqXHR.responseText).message);
      confirmDialog4DeleteReportModal.hide();
    }
  });
}

function passwortZuruecksetzenLaden(mandantId,nutzername) {
  $("#elbook-spinner").attr("style", "");
  $("#mandantFormContainer").attr("style", "display:none");
  $("#mandantPwdResetContainer").attr("style", "");
  $("#mandantPwdResetContainer").empty();
  $.ajax({
    type: "GET",
    url: $("#mandantPwdResetContainer").attr("action")+'passwort/zuruecksetzen/'+mandantId+'/'+nutzername,
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#mandantPwdResetContainer").append(data);
      document.getElementById("passwortZuruecksetzenForm").checkValidity();
      $('.needs-validation').addClass('was-validated');
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#mandantPwdResetContainer").append(jqXHR.responseText);
    }
  });
}

function passwortZuruecksetzen() {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  const confirmDialog4ZuruecksetzenPasswortModal = bootstrap.Modal.getInstance(document.getElementById('confirmDialog4ZuruecksetzenPasswort'));

  const res = document.getElementById("passwortZuruecksetzenForm").checkValidity();
  $('#passwortZuruecksetzenForm .needs-validation').addClass('was-validated');
  if (!res) {
    confirmDialog4ZuruecksetzenPasswortModal.hide();
    return;
  }

  $("#elbook-spinner").attr("style", "");

  const data = $("#passwortZuruecksetzenForm").serializeFormJSON();

  $.ajax({
    type: "POST",
    url: $("#mandantPwdResetContainer").attr("action")+"passwort/zuruecksetzen",
    data: JSON.stringify(data),
    contentType: "application/json",
    dataType: "json",
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      confirmDialog4ZuruecksetzenPasswortModal.hide();
      $("#mandantPwdResetContainer").attr("style", "display:none");
      $("#mandantFormContainer").attr("style", "");
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      confirmDialog4ZuruecksetzenPasswortModal.hide();
      $("#passwortZuruecksetzenFormError").attr("style", "");
      $("#passwortZuruecksetzenFormError").empty();
      $("#passwortZuruecksetzenFormError").append(JSON.parse(jqXHR.responseText).message);
    }
  });
}

function passwortAendern() {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");
  const res = document.getElementById("passwortAendernForm").checkValidity();

  const confirmDialog4AendernPasswortModal = bootstrap.Modal.getInstance(document.getElementById('confirmDialog4AendernPasswort'));

  $('.needs-validation').addClass('was-validated');
  if (!res) {
    confirmDialog4AendernPasswortModal.hide();
    return;
  }

  $("#elbook-spinner").attr("style", "");

  const data = $("#passwortAendernForm").serializeFormJSON();

  $.ajax({
    type: "POST",
    url: $("#passwortAendernFormContainer").attr("action")+"passwort/aendern",
    data: JSON.stringify(data),
    contentType: "application/json",
    dataType: "json",
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      confirmDialog4AendernPasswortModal.hide();
      document.getElementById('logout-form').submit();
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      confirmDialog4AendernPasswortModal.hide();
      $("#passwortAendernFormError").attr("style", "");
      $("#passwortAendernFormError").empty();
      $("#passwortAendernFormError").append(JSON.parse(jqXHR.responseText).message);
    }
  });
}

function hbakartendatentransferSpeichern() {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  const res = document.getElementById("hbakartendatentransferForm").checkValidity();
  $('.needs-validation').addClass('was-validated');
  if (!res) {
    return;
  }

  $("#elbook-spinner").attr("style", "");

  const data = $("#hbakartendatentransferForm").serializeFormJSON();

  $.ajax({
    type: "POST",
    url: $("#hbakartendatentransferFormContainer").attr("action")+"hbakartendatentransfer/speichern",
    data: JSON.stringify(data),
    contentType: "application/json",
    dataType: "json",
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      setTimeout(hbakartendatentransferUebersicht, 0);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#hbakartendatentransferFormError").attr("style", "");
      $("#hbakartendatentransferFormError").empty();
      $("#hbakartendatentransferFormError").append(JSON.parse(jqXHR.responseText).message);
    }
  });
}

function hbakartendatentransferUebersicht(searchValue) {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  if (searchValue === null || searchValue === undefined) {
    searchValue = elbookContext.searchValue;
  }
  elbookContext.searchValue = searchValue?searchValue:'';

  $("#elbook-spinner").attr("style", "");
  $("#hbakartendatentransferFormContainer").attr("style", "display:none");
  $("#hbakartendatentransferTableContainer").attr("style", "");
  $("#hbakartendatentransferTableContainer").empty();
  $.ajax({
    type: "POST",
    url: $("#hbakartendatentransferTableContainer").attr("action")+'hbakartendatentransfer/uebersicht',
    data: 'searchValue='+(searchValue?searchValue:''),
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#hbakartendatentransferTableContainer").append(data);
      $("#search").val(elbookContext.searchValue);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#hbakartendatentransferTableContainer").append(jqXHR.responseText);
    }
  });
}

function hbakartendatenPersonLaden(id, personId) {
  $("#elbook-spinner").attr("style", "");
  $("#hbakartendatenFormContainer").attr("style", "");
  $("#hbakartendatenFormContainer").empty();
  $.ajax({
    type: "GET",
    url: $("#hbakartendatenFormContainer").attr("action")+'hbakartendaten/' + id + '/lade/' + personId,
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#hbakartendatenFormContainer").append(data);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#hbakartendatenFormContainer").append(jqXHR.responseText);
    }
  });
}

function hbakartendatenPersonenUebersicht(hashCode, aktivierungsCode) {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  if (document.getElementById("aktivierungsCodeForm")) {
    const res = document.getElementById("aktivierungsCodeForm").checkValidity();
    $('.needs-validation').addClass('was-validated');
    if (!res) {
      return;
    }
  }

  $("#elbook-spinner").attr("style", "");
  $("#hbakartendatenFormContainer").attr("style", "");
  $("#hbakartendatenFormContainer").empty();
  const data = {
    hashCode: hashCode,
  };
  if (aktivierungsCode) {
    data.aktivierungsCode = aktivierungsCode;
  }
  $.ajax({
    type: "POST",
    data: JSON.stringify(data),
    contentType: "application/json",
    dataType: "json",
    url: $("#hbakartendatenFormContainer").attr("action")+'hbakartendaten/personenuebersicht',
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(res) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#hbakartendatenFormContainer").append(res);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#hbakartendatenFormContainer").append(jqXHR.responseText);
    }
  });
}

function hbakartendatentransferPersonenUebersicht(id) {
  $("#elbook-spinner").attr("style", "");
  $("#hbakartendatentransferTableContainer").attr("style", "display:none");
  $("#hbakartendatentransferFormContainer").attr("style", "");
  $("#hbakartendatentransferFormContainer").empty();
  $.ajax({
    type: "GET",
    url: $("#hbakartendatentransferFormContainer").attr("action")+'hbakartendatentransfer/' + id + '/personenuebersicht' ,
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#hbakartendatentransferFormContainer").append(data);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#hbakartendatentransferFormContainer").append(jqXHR.responseText);
    }
  });
}

function hbakartendatentransferPersonLoeschen() {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  const confirmDialog4DeleteKartendatentransferPersonModal = bootstrap.Modal.getInstance(document.getElementById('confirmDialog4DeleteKartendatentransferPerson'));

  $("#elbook-spinner").attr("style", "");

  $.ajax({
    type: "DELETE",
    url: $("#hbakartendatentransferTableContainer").attr("action")+"hbakartendatentransfer/"+elbookContext.toDeleteKartendatentransfer+"/loeschen/"+elbookContext.toDeleteKartendatentransferPerson,
    contentType: "application/json",
    dataType: "json",
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      confirmDialog4DeleteKartendatentransferPersonModal.hide();
      setTimeout(hbakartendatentransferPersonenUebersicht(elbookContext.toDeleteKartendatentransfer),0);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#hbakartendatentransferTableError").attr("style", "");
      $("#hbakartendatentransferTableError").append(JSON.parse(jqXHR.responseText).message);
      confirmDialog4DeleteKartendatentransferPersonModal.hide();
    }
  });
}

function hbakartendatentransferPersonSpeichern(id, personId) {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  const res = document.getElementById("hbakartendatentransferForm").checkValidity();
  $('.needs-validation').addClass('was-validated');
  if (!res) {
    return;
  }

  $("#elbook-spinner").attr("style", "");

  const data = $("#hbakartendatentransferForm").serializeFormJSON();
  data.unbekanntVerzogen = $("#unbekanntVerzogen").is(':checked');

  $.ajax({
    type: "POST",
    url: $("#hbakartendatentransferFormContainer").attr("action")+"hbakartendatentransfer/"+id+"/speichern/"+personId,
    data: JSON.stringify(data),
    contentType: "application/json",
    dataType: "json",
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      setTimeout(hbakartendatentransferPersonenUebersicht(id), 0);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#hbakartendatentransferFormError").attr("style", "");
      $("#hbakartendatentransferFormError").empty();
      $("#hbakartendatentransferFormError").append(JSON.parse(jqXHR.responseText).message);
    }
  });
}

function hbakartendatentransferPersonLaden(id, personId) {
  $("#elbook-spinner").attr("style", "");
  $("#hbakartendatentransferTableContainer").attr("style", "display:none");
  $("#hbakartendatentransferFormContainer").attr("style", "");
  $("#hbakartendatentransferFormContainer").empty();
  $.ajax({
    type: "GET",
    url: $("#hbakartendatentransferFormContainer").attr("action")+'hbakartendatentransfer/' + id + '/lade/' + personId,
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#hbakartendatentransferFormContainer").append(data);
      document.getElementById("hbakartendatentransferForm").checkValidity();
      $('.needs-validation').addClass('was-validated');
      toggleDeacivate('unbekanntVerzogen', ['strasse', 'hausnummer', 'anschriftenzusatz', 'postleitzahl', 'wohnort']);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#hbakartendatentransferFormContainer").append(jqXHR.responseText);
    }
  });
}

function hbakartendatentransferSenden(sendMailWithActivationCode) {
  const confirmDialog4SendKartendatentransferModal = bootstrap.Modal.getInstance(document.getElementById('confirmDialog4SendKartendatentransfer'));
  const confirmDialog4SendKartendatentransferWithoutActivationCodeModal = bootstrap.Modal.getInstance(document.getElementById('confirmDialog4SendKartendatentransferWithoutActivationCode'));

  $("#elbook-spinner").attr("style", "");
  $("#hbakartendatentransferTableContainer").attr("style", "display:none");
  $("#hbakartendatentransferFormContainer").attr("style", "");
  $("#hbakartendatentransferFormContainer").empty();
  $.ajax({
    type: "GET",
    url: $("#hbakartendatentransferFormContainer").attr("action")+'hbakartendatentransfer/senden/' + elbookContext.toSendKartendatentransfer + '/' + sendMailWithActivationCode,
    success: function() {
      $("#elbook-spinner").attr("style", "display:none");
      if (sendMailWithActivationCode) {
        confirmDialog4SendKartendatentransferModal.hide();
      }
      else {
        confirmDialog4SendKartendatentransferWithoutActivationCodeModal.hide();
      }
      setTimeout(hbakartendatentransferPersonenUebersicht(elbookContext.toSendKartendatentransfer), 0);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      if (sendMailWithActivationCode) {
        confirmDialog4SendKartendatentransferModal.hide();
      }
      else {
        confirmDialog4SendKartendatentransferWithoutActivationCodeModal.hide();
      }
      $("#hbakartendatentransferTableError").append(jqXHR.responseText);
    }
  });
}

function hbakartendatentransferLaden(id) {
  $("#elbook-spinner").attr("style", "");
  $("#hbakartendatentransferTableContainer").attr("style", "display:none");
  $("#hbakartendatentransferFormContainer").attr("style", "");
  $("#hbakartendatentransferFormContainer").empty();
  $.ajax({
    type: "GET",
    url: $("#hbakartendatentransferFormContainer").attr("action")+'hbakartendatentransfer/lade/' + id,
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#hbakartendatentransferFormContainer").append(data);
      document.getElementById("hbakartendatentransferForm").checkValidity();
      $('.needs-validation').addClass('was-validated');
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#hbakartendatentransferFormContainer").append(jqXHR.responseText);
    }
  });
}

function hbakartendatentransferLoeschen() {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  const confirmDialog4DeleteKartendatentransferModal = bootstrap.Modal.getInstance(document.getElementById('confirmDialog4DeleteKartendatentransfer'));

  $("#elbook-spinner").attr("style", "");

  $.ajax({
    type: "DELETE",
    url: $("#hbakartendatentransferTableContainer").attr("action")+"hbakartendatentransfer/loeschen/"+elbookContext.toDeleteKartendatentransfer,
    contentType: "application/json",
    dataType: "json",
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      confirmDialog4DeleteKartendatentransferModal.hide();
      setTimeout(hbakartendatentransferUebersicht,0);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#hbakartendatentransferTableError").attr("style", "");
      $("#hbakartendatentransferTableError").append(JSON.parse(jqXHR.responseText).message);
      confirmDialog4DeleteKartendatentransferModal.hide();
    }
  });
}

function tspLoeschen() {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  const confirmDialog4DeleteTspModal = bootstrap.Modal.getInstance(document.getElementById('confirmDialog4DeleteTsp'));

  $("#elbook-spinner").attr("style", "");
  $.ajax({
    type: "DELETE",
    url: $("#tspTableContainer").attr("action")+"tsp/loeschen/"+elbookContext.toDeleteTsp,
    contentType: "application/json",
    dataType: "json",
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      confirmDialog4DeleteTspModal.hide();
      setTimeout(tspUebersicht,0);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#tspTableError").attr("style", "");
      $("#tspTableError").append(JSON.parse(jqXHR.responseText).message);
      confirmDialog4DeleteTspModal.hide();
    }
  });
}

function tspSpeichern() {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  $("#tspFormError").attr("style", "display:none");
  $("#tspFormSucces").attr("style", "display:none");
  const res = document.getElementById("tspForm").checkValidity();
  $('.needs-validation').addClass('was-validated');
  if (!res) {
    return;
  }

  $("#elbook-spinner").attr("style", "");
  const fData = $("#tspForm").serializeFiles();
  $.ajax({
    type: "POST",
    url: $("#tspFormContainer").attr("action")+"tsp/speichern",
    data: fData,
    cache: false,
    contentType: false,
    processData: false,
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      setTimeout(tspUebersicht, 0);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#tspFormError").attr("style", "");
      $("#tspFormError").empty();
      $("#tspFormError").append(JSON.parse(jqXHR.responseText).message);
    }
  });
}

function tspLoadAllRequest(tspId, antragTyp, aTagId) {
  var aHrefTag = $('#'+aTagId).attr("href");
  aHrefTag = aHrefTag + "/" + tspId + "/" + antragTyp;
  $('#'+aTagId).attr("href", aHrefTag);
  document.getElementById(aTagId).click();
}

function tspLoadRequest(tspId, vorgangsnummer, aTagId) {
  var aHrefTag = $('#'+aTagId).attr("src");
  aHrefTag = aHrefTag + "/" + tspId + "/" + vorgangsnummer;
  $('#'+aTagId).attr("href", aHrefTag);
  document.getElementById(aTagId).click();
}

function tspCheck(tspId) {
  $("#tspFormError").attr("style", "display:none");
  $("#tspFormSucces").attr("style", "display:none");

  const res = document.getElementById("tspForm").checkValidity();
  $('.needs-validation').addClass('was-validated');
  if (!res) {
    return;
  }

  $("#elbook-spinner").attr("style", "");
  $.ajax({
    type: "GET",
    url: $("#tspFormContainer").attr("action")+"tsp/check/"+tspId,
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      if (data) {
        $("#tspFormSucces").attr("style", "");
        $("#tspFormSucces").empty();
        $("#tspFormSucces").append("Verbindungsaufbau erfolgreich!");
      }
      else {
        $("#tspFormError").attr("style", "");
        $("#tspFormError").empty();
        $("#tspFormError").append("Verbindungsaufbau NICHT erfolgreich!");
      }
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#tspFormError").attr("style", "");
      $("#tspFormError").empty();
      $("#tspFormError").append(JSON.parse(jqXHR.responseText).message);
    }
  });
}

function tspLaden(tspId) {
  $("#elbook-spinner").attr("style", "");
  $("#tspTableContainer").attr("style", "display:none");
  $("#tspFormContainer").attr("style", "");
  $("#tspFormContainer").empty();
  $.ajax({
    type: "GET",
    url: $("#tspFormContainer").attr("action")+'tsp/lade/' + tspId,
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#tspFormContainer").append(data);
      document.getElementById("tspForm").checkValidity();
      $('.needs-validation').addClass('was-validated');
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#tspFormContainer").append(jqXHR.responseText);
    }
  });
}

function tspUebersicht() {
  $("#elbook-spinner").attr("style", "");
  $("#tspFormContainer").attr("style", "display:none");
  $("#tspTableContainer").attr("style", "");
  $("#tspTableContainer").empty();
  $.ajax({
    type: "GET",
    url: $("#tspTableContainer").attr("action")+'tsp/uebersicht',
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#tspTableContainer").append(data);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#mandantTtspTableContainerableContainer").append(jqXHR.responseText);
    }
  });
}

function logeintragLaden(id) {
  $("#elbook-spinner").attr("style", "");
  $("#logeintragTableContainer").attr("style", "display:none");
  $("#logeintragFormContainer").attr("style", "");
  $("#logeintragFormContainer").empty();
  $.ajax({
    type: "GET",
    url: $("#logeintragFormContainer").attr("action")+'logeintrag/lade/' + id,
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#logeintragFormContainer").append(data);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#logeintragFormContainer").append(jqXHR.responseText);
    }
  });
}

function logeintragUebersicht(searchValue) {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  if (searchValue === null || searchValue === undefined) {
    searchValue = elbookContext.logentrySearchValue;
  }
  elbookContext.logentrySearchValue = searchValue?searchValue:'';

  $("#elbook-spinner").attr("style", "");
  $("#logeintragFormContainer").attr("style", "display:none");
  $("#logeintragTableContainer").attr("style", "");
  $("#logeintragTableContainer").empty();
  $.ajax({
    type: "POST",
    url: $("#logeintragTableContainer").attr("action")+'logeintrag/uebersicht',
    data: 'searchValue='+(searchValue?searchValue:''),
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#logeintragTableContainer").append(data);
      $("#search").val(elbookContext.logentrySearchValue);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#logeintragTableContainer").append(jqXHR.responseText);
    }
  });
}

function stammdatencertimportUebersicht() {
  $("#elbook-spinner").attr("style", "");
  $("#stammdatencertimportTableContainer").attr("style", "");
  $("#stammdatencertimportTableContainer").empty();
  $.ajax({
    type: "GET",
    url: $("#stammdatencertimportTableContainer").attr("action")+'stammdatencertimport/uebersicht',
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#stammdatencertimportTableContainer").append(data);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#stammdatencertimportTableContainer").append(jqXHR.responseText);
    }
  });
}

function stammdatencertimportZusammenfassung(jobId) {
  const showDialog4ResultSummaryModal = new bootstrap.Modal(document.getElementById("showDialog4ResultSummary"), {});

  $("#elbook-spinner").attr("style", "");
  $.ajax({
    type: "GET",
    url: $("#stammdatencertimportTableContainer").attr("action")+'stammdatencertimport/zusammenfassen/ergebnisse/' + jobId,
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      showDialog4ResultSummaryModal.show();
      $('#showDialog4ResultSummary #loadError').text('');
      $('#showDialog4ResultSummary #errorCount').text(data.errorCount+' Fehler');
      $('#showDialog4ResultSummary #insertCount').text(data.insertCount+' Einfügungen');
      $('#showDialog4ResultSummary #updateCount').text(data.updateCount+' Änderungen');
      $('#showDialog4ResultSummary #certUpdateCount').text(data.certUpdateCount+' Zertifikatsaktualisierungen');
      $('#showDialog4ResultSummary #deleteCount').text(data.deleteCount+' Löschungen');
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      showDialog4ResultSummaryModal.show();
      $('#showDialog4ResultSummary #loadError').text(jqXHR.responseText);
      $('#showDialog4ResultSummary #errorCount').text('0 Fehler');
      $('#showDialog4ResultSummary #insertCount').text('0 Einfügungen');
      $('#showDialog4ResultSummary #updateCount').text('0 Änderungen');
      $('#showDialog4ResultSummary #certUpdateCount').text('0 Zertifikatsaktualisierungen');
      $('#showDialog4ResultSummary #deleteCount').text('0 Löschungen');
    }
  });
}

function vzdConnectionUebersicht(searchValue) {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  if (searchValue === null || searchValue === undefined) {
    searchValue = elbookContext.connectionSearchValue;
  }
  elbookContext.connectionSearchValue = searchValue ? searchValue : '';

  $("#elbook-spinner").attr("style", "");
  $("#vzdConnectionTableContainer").attr("style", "");
  $("#vzdConnectionTableContainer").empty();
  $.ajax({
    type: "POST",
    url: $("#vzdConnectionTableContainer").attr("action") + 'clientconnection/uebersicht',
    data: 'searchValue=' + (searchValue ? searchValue : ''),
    beforeSend: function (xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function (data) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#vzdConnectionTableContainer").append(data);
      $("#search").val(elbookContext.connectionSearchValue);
    },
    error: function (jqXHR, textStatus, errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#vzdConnectionTableContainer").append(jqXHR.responseText);
    }
  });
}

function vzdConnectionLoeschen() {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  const confirmDialog4DeleteVzdConnectionModal = bootstrap.Modal.getInstance(document.getElementById('confirmDialog4DeleteVzdConnection'));

  $("#elbook-spinner").attr("style", "");

  $.ajax({
    type: "DELETE",
    url: $("#vzdConnectionTableContainer").attr("action")+"clientconnection/loeschen/"+elbookContext.toDeleteConnectionId+"/"+elbookContext.toDeleteConnectionMutex,
    contentType: "application/json",
    dataType: "json",
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      confirmDialog4DeleteVzdConnectionModal.hide();
      setTimeout(vzdConnectionUebersicht,0);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#vzdConnectionTableError").attr("style", "");
      $("#vzdConnectionTableError").append(JSON.parse(jqXHR.responseText).message);
      confirmDialog4DeleteVzdConnectionModal.hide();
    }
  });
}

function threadUebersicht() {
  $("#elbook-spinner").attr("style", "");
  $("#threadTableContainer").attr("style", "");
  $("#threadTableContainer").empty();
  $.ajax({
    type: "GET",
    url: $("#threadTableContainer").attr("action")+'thread/uebersicht',
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#threadTableContainer").append(data);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#threadTableContainer").append(jqXHR.responseText);
    }
  });
}

function threadStarten(anzahl, anzahlThreads) {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  $("#elbook-spinner").attr("style", "");
  $("#threadTableContainer").attr("style", "");
  $("#threadTableContainer").empty();
  $.ajax({
    type: "POST",
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    data: 'anzahl='+anzahl+'&anzahlThreads='+anzahlThreads,
    url: $("#threadTableContainer").attr("action")+'thread/starten',
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#threadTableContainer").append(data);
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#threadTableContainer").append(jqXHR.responseText);
    }
  });
}

function enable2FA(){
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  const confirmDialog4Activate2FAModal = bootstrap.Modal.getInstance(document.getElementById('confirmDialog4Activate2FA'));

  $("#elbook-spinner").attr("style", "");

  $.ajax({
    type: "POST",
    url: $("#modify2FAContainer").attr("action") + "mandant/enable2FA",
    contentType: "application/json",
    dataType: "json",
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#activate2FAContainerFormError").attr("style", "display:none");
      $("#elbook-spinner").attr("style", "display:none");
      confirmDialog4Activate2FAModal.hide();
      $("#qrCode2FA").empty();
      $("#qrCode2FA").append('<img src="' + data.qrCodeLink + '" />').show();
      $('#activate2FAContainerFormContainer').show();
      $('#activate2FAContainerFormContainer #secret2FA').val(data.secret2FA);
      $('#activate2FAButton').hide();
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      confirmDialog4Activate2FAModal.hide();
      $("#activate2FAContainerFormError").attr("style", "");
      $("#activate2FAContainerFormError").empty();
      $("#activate2FAContainerFormError").append("Beim Erstellen des QR-Codes ist ein Fehler aufgetreten");
    }
  });
}

function enableCancel2FA() {
  document.location.reload();
}

function enableFinish2FA() {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  const res = document.getElementById("activate2FAContainerForm").checkValidity();
  $('#activate2FAContainerForm .needs-validation').addClass('was-validated');
  if (!res) {
    return;
  }

  $("#elbook-spinner").attr("style", "");

  var req = {};
  req.secret2FA = $('#secret2FA').val();
  req.currentElbookPwd = $('#elbookPwd4Activate').val();
  req.twoFaCode = $('#2faCode').val();

  $.ajax({
    type: "POST",
    url: $("#modify2FAContainer").attr("action") + "mandant/enableFinish2FA",
    contentType: "application/json",
    dataType: "json",
    data: JSON.stringify(req),
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      document.location.reload();
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      $("#activate2FAContainerFormError").attr("style", "");
      $("#activate2FAContainerFormError").empty();
      $("#activate2FAContainerFormError").append("Beim Abschliessen der 2-Faktor-Aktivierung ist ein Fehler aufgetreten");
    }
  });
}

function disableFinish2FA() {
  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  const confirmDialog4Deactivate2FAModal = bootstrap.Modal.getInstance(document.getElementById('confirmDialog4Deactivate2FA'));

  const res = document.getElementById("deactivate2FAContainerForm").checkValidity();
  $('#deactivate2FAContainerForm .needs-validation').addClass('was-validated');
  if (!res) {
    confirmDialog4Deactivate2FAModal.hide();
    return;
  }

  $("#elbook-spinner").attr("style", "");

  var req = {};
  req.currentElbookPwd = $('#elbookPwd4Deactivate').val();

  $.ajax({
    type: "POST",
    url: $("#modify2FAContainer").attr("action") + "mandant/disableFinish2FA",
    contentType: "application/json",
    dataType: "json",
    data: JSON.stringify(req),
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(data) {
      $("#elbook-spinner").attr("style", "display:none");
      confirmDialog4Deactivate2FAModal.hide();
      document.location.reload();
    },
    error: function(jqXHR,textStatus,errorThrown) {
      $("#elbook-spinner").attr("style", "display:none");
      confirmDialog4Deactivate2FAModal.hide();
      $("#deactivate2FAContainerFormError").attr("style", "");
      $("#deactivate2FAContainerFormError").empty();
      $("#deactivate2FAContainerFormError").append("Beim Abschliessen der 2-Faktor-Deaktivierung ist ein Fehler aufgetreten");
    }
  });
}

function holderAttrFileSenden() {

  $("#holderattributFormularError").attr("style", "display: none");
  $("#holderattributFormularError").empty();
  $("#holderattributFormularResult").empty();

  const token = $("meta[name='_csrf']").attr("content");
  const header = $("meta[name='_csrf_header']").attr("content");

  const res = document.getElementById("holderAttributForm").checkValidity();
  $('.needs-validation').addClass('was-validated');
  if (!res) {
    return;
  }

  $(".spinner-border").attr("style", "");
  const fData = $("#holderAttributForm").serializeFiles();

  $("#holderAttributForm").hide();

  $.ajax({
    type: "POST",
    url: $("#holderattributContainer").attr("action") + "holderattribut/jsondatei",
    data: fData,
    cache: false,
    contentType: false,
    processData: false,
    beforeSend: function (xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function (data) {
      $(".spinner-border").attr("style", "display:none");
      $("#holderAttributForm").show();
      $("#holderAttributForm").trigger("reset");
      $("#holderattributFormularResult").empty();
      $("#holderattributFormularResult").append(JSON.stringify(data, null, 2));
    },
    error: function (jqXHR, textStatus, errorThrown) {
      $(".spinner-border").attr("style", "display:none");
      $("#holderAttributForm").show();
      $("#holderattributFormularError").attr("style", "");
      $("#holderattributFormularError").empty();
      $("#holderattributFormularError").append(JSON.parse(jqXHR.responseText).message);
    }
  });
}