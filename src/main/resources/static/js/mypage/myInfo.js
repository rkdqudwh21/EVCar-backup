'use strict';

document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('myInfoForm');
    const editModeButton = document.getElementById('editModeButton');
    const saveButton = document.getElementById('saveButton');
    const cancelButton = document.getElementById('cancelButton');

    if (!form || !editModeButton || !saveButton || !cancelButton) {
        return;
    }

    const MIN_VEHICLE_YEAR = 1900;
    const MIN_PHONE_DIGITS = 10;
    const MAX_PHONE_DIGITS = 15;
    const PASSWORD_REGEX = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,20}$/;

    const editableInputs = Array.from(form.querySelectorAll('[data-editable="true"]'));
    const genderInputs = Array.from(form.querySelectorAll('input[name="gender"]'));
    const hasVehicleInputs = Array.from(form.querySelectorAll('input[name="hasVehicle"]'));
    const ownedVehicleFields = Array.from(form.querySelectorAll('.ev-vehicle-owned-field'));
    const hasVehicleGroup = document.getElementById('hasVehicleGroup');

    const fields = {
        name: form.querySelector('input[name="name"]'),
        birthDate: form.querySelector('input[name="birthDate"]'),
        phone: form.querySelector('input[name="phone"]'),
        address: form.querySelector('input[name="address"]'),
        addressDetail: form.querySelector('input[name="addressDetail"]'),
        email: form.querySelector('input[name="email"]'),
        currentPassword: form.querySelector('input[name="currentPassword"]'),
        newPassword: form.querySelector('input[name="newPassword"]'),
        newPasswordConfirm: form.querySelector('input[name="newPasswordConfirm"]'),
        vehicleModel: form.querySelector('input[name="vehicleModel"]'),
        vehicleYear: form.querySelector('input[name="vehicleYear"]'),
        drivingDistance: form.querySelector('input[name="drivingDistance"]')
    };

    const phoneInput = document.getElementById('phone');
    const initialValues = new Map();

    const showFormMessage = (message, type = 'error') => {
        let messageBox = document.getElementById('formMessageBox');

        if (!messageBox) {
            messageBox = document.createElement('div');
            messageBox.id = 'formMessageBox';
            form.prepend(messageBox);
        }

        messageBox.className = `ev-form-message ev-form-message--${type}`;
        messageBox.textContent = message;
    };

    const clearFormMessage = () => {
        const messageBox = document.getElementById('formMessageBox');
        if (messageBox) {
            messageBox.remove();
        }
    };

    const showServerMessages = () => {
        const infoErrorMessage = window.infoErrorMessage ?? null;
        const message = window.message ?? null;

        if (infoErrorMessage) {
            showFormMessage(infoErrorMessage, 'error');
            return;
        }

        if (message) {
            showFormMessage(message, 'success');
        }
    };

	const alertAndFocus = (message, element) => {
	    showFormMessage(message, 'error');

	    const messageBox = document.getElementById('formMessageBox');
	    if (messageBox) {
	        messageBox.scrollIntoView({ behavior: 'smooth', block: 'center' });
	    }

	    return false;
	};

    const getInputValue = (input) => input?.value?.trim() ?? '';

    const sanitizePhoneInput = (value) => {
        let raw = (value ?? '').trim();
        raw = raw.replace(/[^\d+]/g, '');

        if (raw.startsWith('+')) {
            const digitsOnly = raw.substring(1).replace(/\D/g, '');
            return `+${digitsOnly}`;
        }

        return raw.replace(/\D/g, '');
    };

    const normalizePhoneForValidation = (value) => {
        const raw = (value ?? '').trim();

        if (!raw) {
            return '';
        }

        if (raw.startsWith('+')) {
            const digitsOnly = raw.substring(1).replace(/\D/g, '');
            return `+${digitsOnly}`;
        }

        return raw.replace(/\D/g, '');
    };

    const getPhoneDigits = (value) => {
        return (value ?? '').replace(/\D/g, '');
    };

    const isValidPhoneFormat = (value) => {
        return /^\+?\d+$/.test(value);
    };

    const isValidVehicleYearMonthFormat = (value) => {
        return /^\d{4}-(0[1-9]|1[0-2])$/.test(value);
    };

    const getCurrentYearMonth = () => {
        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');
        return `${year}-${month}`;
    };

    const sanitizeVehicleYearInput = (value) => {
        const digitsOnly = (value ?? '').replace(/\D/g, '').slice(0, 6);

        if (digitsOnly.length <= 4) {
            return digitsOnly;
        }

        return `${digitsOnly.slice(0, 4)}-${digitsOnly.slice(4, 6)}`;
    };

    const isFutureVehicleYearMonth = (value) => {
        if (!isValidVehicleYearMonthFormat(value)) {
            return false;
        }

        return value > getCurrentYearMonth();
    };

    const isTooEarlyVehicleYearMonth = (value) => {
        if (!isValidVehicleYearMonthFormat(value)) {
            return false;
        }

        const year = Number(value.substring(0, 4));
        return year < MIN_VEHICLE_YEAR;
    };

    const getHasVehicleValue = () => {
        const checked = form.querySelector('input[name="hasVehicle"]:checked');
        return checked ? checked.value : '';
    };

    const isVehicleOwned = () => {
        const value = getHasVehicleValue();
        return value === 'yes' || value === 'Y' || value === 'y';
    };

    const isEditing = () => !saveButton.classList.contains('ev-myinfo-hidden');

    const saveInitialValues = () => {
        form.querySelectorAll('input').forEach((input) => {
            if (input.type === 'radio') {
                initialValues.set(`${input.name}:${input.value}`, input.checked);
                return;
            }

            initialValues.set(input.name, input.value ?? '');
        });
    };

    const restoreInitialValues = () => {
        form.querySelectorAll('input').forEach((input) => {
            if (input.type === 'radio') {
                input.checked = Boolean(initialValues.get(`${input.name}:${input.value}`));
                return;
            }

            input.value = initialValues.get(input.name) ?? '';
        });

        updateOwnedVehicleFieldsState();
        clearZeroDrivingDistanceWhenEmpty();
    };

    const clearOwnedVehicleFields = () => {
        ownedVehicleFields.forEach((field) => {
            field.value = '';
        });
    };

    const clearPasswordFields = () => {
        if (fields.currentPassword) fields.currentPassword.value = '';
        if (fields.newPassword) fields.newPassword.value = '';
        if (fields.newPasswordConfirm) fields.newPasswordConfirm.value = '';
    };

    const clearZeroDrivingDistanceWhenEmpty = () => {
        if (!fields.drivingDistance) {
            return;
        }

        const rawValue = getInputValue(fields.drivingDistance);

        if (!isVehicleOwned() && rawValue === '0') {
            fields.drivingDistance.value = '';
        }
    };

    const updateOwnedVehicleFieldsState = () => {
        const vehicleOwned = isVehicleOwned();
        const editing = isEditing();

        ownedVehicleFields.forEach((field) => {
            if (!editing) {
                field.readOnly = true;
                field.disabled = false;
                return;
            }

            if (!vehicleOwned) {
                field.value = '';
                field.readOnly = true;
                field.disabled = true;
                return;
            }

            field.readOnly = false;
            field.disabled = false;
        });
    };

    const toggleActionButtons = (editing) => {
        editModeButton.classList.toggle('ev-myinfo-hidden', editing);
        saveButton.classList.toggle('ev-myinfo-hidden', !editing);
        cancelButton.classList.toggle('ev-myinfo-hidden', !editing);
    };

    const updateEditableFieldsState = (editing) => {
        editableInputs.forEach((input) => {
            if (ownedVehicleFields.includes(input)) {
                return;
            }

            input.readOnly = !editing;
            input.disabled = false;
        });

        genderInputs.forEach((input) => {
            input.disabled = true;
        });

        hasVehicleInputs.forEach((input) => {
            input.disabled = !editing;
        });

        if (hasVehicleGroup) {
            hasVehicleGroup.classList.toggle('is-disabled', !editing);
        }

        updateOwnedVehicleFieldsState();
    };

    const setEditMode = (editing) => {
        toggleActionButtons(editing);
        updateEditableFieldsState(editing);

        if (!editing) {
            clearPasswordFields();
        }
    };

    const validateRequiredField = (input, message) => {
        if (!input || getInputValue(input)) {
            return true;
        }

        return alertAndFocus(message, input);
    };

    const validatePhone = () => {
        const rawPhone = getInputValue(fields.phone);
        const normalizedPhone = normalizePhoneForValidation(rawPhone);
        const phoneDigits = getPhoneDigits(normalizedPhone);

        if (!rawPhone) {
            return alertAndFocus('전화번호를 입력해주세요.', fields.phone);
        }

        if (!isValidPhoneFormat(normalizedPhone)) {
            return alertAndFocus('전화번호는 숫자와 맨 앞의 + 기호만 사용할 수 있습니다.', fields.phone);
        }

        if (phoneDigits.length < MIN_PHONE_DIGITS || phoneDigits.length > MAX_PHONE_DIGITS) {
            return alertAndFocus('전화번호는 국가번호 포함 10~15자리 숫자로 입력해주세요.', fields.phone);
        }

        return true;
    };

    const validateBasicFields = () => {
        if (!validateRequiredField(fields.name, '이름을 입력해주세요.')) {
            return false;
        }

        if (!validateRequiredField(fields.birthDate, '생년월일을 입력해주세요.')) {
            return false;
        }

        if (!validatePhone()) {
            return false;
        }

        if (!validateRequiredField(fields.address, '주소를 입력해주세요.')) {
            return false;
        }

        if (!validateRequiredField(fields.email, '이메일을 입력해주세요.')) {
            return false;
        }

        return true;
    };

    const validateVehicleYear = () => {
        const vehicleYear = getInputValue(fields.vehicleYear);

        if (!vehicleYear) {
            return alertAndFocus('보유차종 연식을 입력해주세요.', fields.vehicleYear);
        }

        if (!isValidVehicleYearMonthFormat(vehicleYear)) {
            return alertAndFocus('보유차종 연식은 YYYY-MM 형식이며, 월은 01~12로 입력해주세요.', fields.vehicleYear);
        }

        if (isTooEarlyVehicleYearMonth(vehicleYear)) {
            return alertAndFocus('보유차량 연식은 1900년 이후로 입력해주세요.', fields.vehicleYear);
        }

        if (isFutureVehicleYearMonth(vehicleYear)) {
            return alertAndFocus('보유차종 연식은 현재 연월보다 클 수 없습니다.', fields.vehicleYear);
        }

        return true;
    };

    const validateDrivingDistance = () => {
        const drivingDistance = getInputValue(fields.drivingDistance);

        if (!drivingDistance) {
            return alertAndFocus('주행거리를 입력해주세요.', fields.drivingDistance);
        }

        const parsedDistance = Number(drivingDistance);

        if (Number.isNaN(parsedDistance)) {
            return alertAndFocus('주행거리는 숫자로 입력해주세요.', fields.drivingDistance);
        }

        if (parsedDistance < 0) {
            return alertAndFocus('주행거리는 0 이상이어야 합니다.', fields.drivingDistance);
        }

        return true;
    };

    const validateOwnedVehicleFields = () => {
        if (!isVehicleOwned()) {
            return true;
        }

        if (!validateRequiredField(fields.vehicleModel, '보유 차량명을 입력해주세요.')) {
            return false;
        }

        if (!validateVehicleYear()) {
            return false;
        }

        if (!validateDrivingDistance()) {
            return false;
        }

        return true;
    };

    const isAnyFieldChanged = () => {
        return Array.from(form.querySelectorAll('input')).some((input) => {
            if (input.type === 'password') {
                return false;
            }

            if (input.type === 'radio') {
                return Boolean(initialValues.get(`${input.name}:${input.value}`)) !== input.checked;
            }

            return (initialValues.get(input.name) ?? '') !== (input.value ?? '');
        });
    };

    const validatePasswordChange = () => {
        const currentPassword = getInputValue(fields.currentPassword);
        const newPassword = getInputValue(fields.newPassword);
        const newPasswordConfirm = getInputValue(fields.newPasswordConfirm);
        const isPasswordChangeRequested = Boolean(newPassword || newPasswordConfirm);
        const isInfoChanged = isAnyFieldChanged();

        if (!isPasswordChangeRequested && !isInfoChanged) {
            return alertAndFocus('변경된 정보가 없습니다.', null);
        }

        if ((isPasswordChangeRequested || isInfoChanged) && !currentPassword) {
            return alertAndFocus('현재 비밀번호를 입력해주세요.', fields.currentPassword);
        }

        if (!isPasswordChangeRequested) {
            return true;
        }

        if (!newPassword || !newPasswordConfirm) {
            return alertAndFocus('새 비밀번호와 새 비밀번호 확인을 모두 입력해주세요.', fields.newPassword);
        }

        if (!PASSWORD_REGEX.test(newPassword)) {
            return alertAndFocus('새 비밀번호는 영문, 숫자를 포함하여 8~20자로 입력해주세요.', fields.newPassword);
        }

        if (newPassword !== newPasswordConfirm) {
            return alertAndFocus('새 비밀번호와 새 비밀번호 확인이 일치하지 않습니다.', fields.newPasswordConfirm);
        }

        return true;
    };

    const prepareSubmit = () => {
        form.querySelectorAll('input').forEach((input) => {
            if (input.name === 'gender') {
                input.disabled = true;
                return;
            }

            input.disabled = false;
        });

        if (fields.phone) {
            fields.phone.value = getInputValue(fields.phone);
        }

        if (fields.vehicleYear) {
            fields.vehicleYear.value = getInputValue(fields.vehicleYear);
        }

        if (!isVehicleOwned()) {
            clearOwnedVehicleFields();
        }

        clearFormMessage();
    };

    const validateForm = () => {
        clearFormMessage();

        if (!validatePasswordChange()) {
            return false;
        }

        if (!validateBasicFields()) {
            return false;
        }

        if (!validateOwnedVehicleFields()) {
            return false;
        }

        return true;
    };

    if (phoneInput) {
        phoneInput.addEventListener('input', () => {
            phoneInput.value = sanitizePhoneInput(phoneInput.value);
        });

        phoneInput.addEventListener('blur', () => {
            phoneInput.value = sanitizePhoneInput(phoneInput.value);
        });
    }

    if (fields.vehicleYear) {
        fields.vehicleYear.addEventListener('input', () => {
            fields.vehicleYear.value = sanitizeVehicleYearInput(fields.vehicleYear.value);
        });

        fields.vehicleYear.addEventListener('blur', () => {
            fields.vehicleYear.value = sanitizeVehicleYearInput(fields.vehicleYear.value);
        });
    }

    hasVehicleInputs.forEach((input) => {
        input.addEventListener('change', () => {
            clearFormMessage();

            if (!isVehicleOwned()) {
                clearOwnedVehicleFields();
            }

            updateOwnedVehicleFieldsState();
            clearZeroDrivingDistanceWhenEmpty();
        });
    });

    editModeButton.addEventListener('click', () => {
        clearFormMessage();
        saveInitialValues();
        setEditMode(true);
    });

    cancelButton.addEventListener('click', () => {
        clearFormMessage();
        restoreInitialValues();
        setEditMode(false);
    });

    form.addEventListener('submit', (event) => {
        if (!validateForm()) {
            event.preventDefault();
            return;
        }

        prepareSubmit();
    });

    saveInitialValues();
    setEditMode(false);
    clearZeroDrivingDistanceWhenEmpty();
    showServerMessages();
});