const API = "";
const state = {
    token: localStorage.getItem("kl_token"),
    role: localStorage.getItem("kl_role"),
    regNo: localStorage.getItem("kl_reg_no"),
};

const rupees = value => `Rs. ${Number(value || 0).toLocaleString("en-IN")}`;

function headers(json = true) {
    const h = {};
    if (json) h["Content-Type"] = "application/json";
    if (state.token) h.Authorization = `Bearer ${state.token}`;
    return h;
}

async function api(path, options = {}) {
    let response;
    try {
        response = await fetch(API + path, {
            ...options,
            headers: { ...headers(options.body !== undefined), ...(options.headers || {}) },
        });
    } catch {
        throw new Error("Server is not reachable. Please wait and try again.");
    }
    if (!response.ok) {
        const text = await response.text();
        let err = {};
        try {
            err = text ? JSON.parse(text) : {};
        } catch {
            err = { message: text };
        }
        const fieldErrors = err.errors ? Object.values(err.errors).join(", ") : "";
        throw new Error(fieldErrors || err.message || response.statusText || `Request failed with ${response.status}`);
    }
    return response.status === 204 ? null : response.json();
}

async function downloadFile(path, filename) {
    const response = await fetch(API + path, { headers: headers(false) });
    if (!response.ok) throw new Error(response.status === 403 ? "Please login again before downloading this document" : "Download failed");
    const blob = await response.blob();
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
}

function toast(message, type = "success") {
    const wrap = document.querySelector("#toastWrap") || createToastWrap();
    const el = document.createElement("div");
    el.className = `toast align-items-center text-bg-${type} border-0`;
    el.innerHTML = `<div class="d-flex"><div class="toast-body">${message}</div><button class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button></div>`;
    wrap.appendChild(el);
    new bootstrap.Toast(el, { delay: 3200 }).show();
}

function createToastWrap() {
    const wrap = document.createElement("div");
    wrap.id = "toastWrap";
    wrap.className = "toast-container position-fixed bottom-0 end-0 p-3";
    document.body.appendChild(wrap);
    return wrap;
}

function setTheme(theme) {
    document.documentElement.dataset.theme = theme;
    localStorage.setItem("kl_theme", theme);
}

function saveAuth(data) {
    state.token = data.token;
    state.role = data.role;
    state.regNo = data.regNo;
    localStorage.setItem("kl_token", data.token);
    localStorage.setItem("kl_role", data.role);
    if (data.regNo) localStorage.setItem("kl_reg_no", data.regNo);
}

function logout() {
    localStorage.removeItem("kl_token");
    localStorage.removeItem("kl_role");
    localStorage.removeItem("kl_reg_no");
    location.href = "/pages/login.html";
}

document.addEventListener("DOMContentLoaded", () => {
    setTheme(localStorage.getItem("kl_theme") || "light");
    document.querySelectorAll("[data-theme-toggle]").forEach(btn => {
        btn.addEventListener("click", () => setTheme(document.documentElement.dataset.theme === "dark" ? "light" : "dark"));
    });
    document.querySelectorAll("[data-logout]").forEach(btn => btn.addEventListener("click", logout));
    initPublicPages();
    initAuthForms();
    initStudentDashboard();
    initAdminDashboard();
});

async function initPublicPages() {
    await initCourses();
    await initAnnouncements();
}

async function initCourses() {
    const nodes = document.querySelectorAll("[data-courses]");
    const selects = document.querySelectorAll("[data-course-select]");
    if (!nodes.length && !selects.length) return;
    try {
        const courses = await api("/api/courses");
        nodes.forEach(node => node.innerHTML = courses.map(courseCard).join(""));
        selects.forEach(select => {
            select.innerHTML = `<option value="">Select course</option>` + courses.map(c =>
                `<option value="${c.courseId}" data-fee="${c.totalFee}">${c.department} - ${rupees(c.totalFee)}</option>`).join("");
            select.addEventListener("change", () => {
                const option = select.options[select.selectedIndex];
                const target = document.querySelector(select.dataset.courseSelect);
                if (target && option.dataset.fee) target.innerHTML = feeBreakdown(Number(option.dataset.fee), 0, 0);
            });
        });
    } catch (e) {
        toast(e.message, "danger");
    }
}

function courseCard(c) {
    return `<div class="col-md-6 col-xl-4"><div class="glass panel p-4 h-100">
        <div class="d-flex justify-content-between gap-3"><h3 class="h5">${c.department}</h3><span class="badge text-bg-primary">${c.availableSeats} seats</span></div>
        <p class="text-muted-portal mb-2">${c.courseName}</p><div class="fs-4 fw-bold">${rupees(c.totalFee)}</div><small>${c.duration}</small>
    </div></div>`;
}

function feeBreakdown(courseFee, hostelFee, messFee) {
    const exam = 6000, lab = 9000, library = 4000;
    const tuition = Math.max(courseFee - exam - lab - library, 0);
    const total = courseFee + hostelFee + messFee;
    return `<div class="row g-2 small">
        ${line("Tuition Fee", tuition)}${line("Exam Fee", courseFee ? exam : 0)}${line("Lab Fee", courseFee ? lab : 0)}
        ${line("Library Fee", courseFee ? library : 0)}${line("Hostel Fee", hostelFee)}${line("Mess Fee", messFee)}
        <div class="col-12 border-top pt-2 mt-2 d-flex justify-content-between fw-bold"><span>Total</span><span>${rupees(total)}</span></div>
    </div>`;
}

function line(label, value) {
    return `<div class="col-12 d-flex justify-content-between"><span>${label}</span><span>${rupees(value)}</span></div>`;
}

function initAuthForms() {
    const register = document.querySelector("#registerForm");
    if (register) {
        register.addEventListener("submit", async e => {
            e.preventDefault();
            const body = Object.fromEntries(new FormData(register).entries());
            body.courseId = Number(body.courseId);
            try {
                const data = await api("/api/auth/register", { method: "POST", body: JSON.stringify(body) });
                toast(`Application submitted. Register No: ${data.regNo}`);
                setTimeout(() => location.href = "/pages/login.html", 1400);
            } catch (err) { toast(err.message, "danger"); }
        });
    }
    document.querySelectorAll("[data-login-form]").forEach(form => {
        form.addEventListener("submit", async e => {
            e.preventDefault();
            try {
                const data = await api("/api/auth/login", { method: "POST", body: JSON.stringify(Object.fromEntries(new FormData(form).entries())) });
                saveAuth(data);
                location.href = data.role === "ADMIN" ? "/pages/admin-dashboard.html" : "/pages/student-dashboard.html";
            } catch (err) { toast(err.message, "danger"); }
        });
    });
    const forgot = document.querySelector("#forgotForm");
    if (forgot) {
        forgot.addEventListener("submit", async e => {
            e.preventDefault();
            const data = await api("/api/auth/forgot-password", { method: "POST", body: JSON.stringify(Object.fromEntries(new FormData(forgot).entries())) });
            toast(data.message);
        });
    }
    const reset = document.querySelector("#resetForm");
    if (reset) {
        reset.addEventListener("submit", async e => {
            e.preventDefault();
            const data = await api("/api/auth/reset-password", { method: "POST", body: JSON.stringify(Object.fromEntries(new FormData(reset).entries())) });
            toast(data.message);
        });
    }
}

async function initAnnouncements() {
    const node = document.querySelector("[data-announcements]");
    if (!node) return;
    const list = await api("/api/announcements");
    node.innerHTML = renderAnnouncements(list);
}

function initStudentDashboard() {
    const shell = document.querySelector("#studentDashboard");
    if (!shell) return;
    if (!state.token || state.role !== "STUDENT") {
        location.href = "/pages/login.html";
        return;
    }
    bindDashboardNav(shell, renderStudentView);
    renderStudentView("dashboard");
}

function initAdminDashboard() {
    const shell = document.querySelector("#adminDashboard");
    if (!shell) return;
    if (!state.token || state.role !== "ADMIN") {
        location.href = "/pages/admin-login.html";
        return;
    }
    bindDashboardNav(shell, renderAdminView);
    renderAdminView("analytics");
    setInterval(() => {
        const active = shell.querySelector(".sidebar .nav-link.active")?.dataset.view;
        if (["analytics", "students", "hostel"].includes(active)) renderAdminView(active, true);
    }, 20000);
}

function bindDashboardNav(shell, renderer) {
    shell.querySelectorAll("[data-view]").forEach(link => {
        link.addEventListener("click", e => {
            e.preventDefault();
            shell.querySelectorAll("[data-view]").forEach(nav => nav.classList.remove("active"));
            link.classList.add("active");
            renderer(link.dataset.view);
        });
    });
}

function content() {
    return document.querySelector("#dashboardContent");
}

function loading(title = "Loading") {
    content().innerHTML = `<div class="glass panel p-5"><div class="loading mb-3"></div><h1 class="h5">${title}</h1></div>`;
}

async function renderStudentView(view) {
    try {
        loading();
        if (view === "dashboard") return studentDashboard();
        if (view === "courses") return studentCourses();
        if (view === "payments") return studentPayments();
        if (view === "hostel") return studentHostel();
        if (view === "profile") return studentProfile();
        if (view === "announcements") return studentAnnouncements();
    } catch (e) {
        toast(e.message, "danger");
    }
}

async function studentDashboard() {
    const profile = await api(`/api/students/${state.regNo}`);
    const fees = await api(`/api/fees/history/${state.regNo}`);
    document.querySelector("#dashboardTitle").textContent = "Student Dashboard";
    document.querySelector("#dashboardSubtitle").textContent = `${profile.regNo} | ${profile.department} | ${profile.status}`;
    content().innerHTML = `<div class="row g-3 mb-4">
        ${metric(["Admission", badge(profile.status), "fa-user-check"])}
        ${metric(["Fee Balance", rupees(fees[0]?.balanceFee || 0), "fa-file-invoice"])}
        ${metric(["Paid Amount", rupees(fees[0]?.paidFee || 0), "fa-credit-card"])}
        ${metric(["Department", profile.department || "-", "fa-building-columns"])}
    </div>
    <div class="glass panel p-4"><h2 class="h5">Semester Fee Status</h2><div class="table-responsive"><table class="table align-middle">
    <thead><tr><th>Semester</th><th>Total</th><th>Paid</th><th>Balance</th><th>Status</th></tr></thead>
    <tbody>${fees.map(f => `<tr><td>${f.semester}</td><td>${rupees(f.totalFee)}</td><td>${rupees(f.paidFee)}</td><td>${rupees(f.balanceFee)}</td><td>${badge(f.paymentStatus)}</td></tr>`).join("")}</tbody>
    </table></div></div>`;
}

async function studentCourses() {
    const courses = await api("/api/courses");
    document.querySelector("#dashboardTitle").textContent = "Courses";
    document.querySelector("#dashboardSubtitle").textContent = "Course fees and seat availability";
    content().innerHTML = `<div class="row g-3">${courses.map(courseCard).join("")}</div>`;
}

async function studentPayments() {
    document.querySelector("#dashboardTitle").textContent = "Fee Payments";
    document.querySelector("#dashboardSubtitle").textContent = "Pay fees and download receipts";
    content().innerHTML = `<div class="row g-4"><div class="col-lg-5"><form id="feeForm" class="glass panel p-4">
        <h2 class="h5 mb-3">Pay Semester Fee</h2>
        <input class="form-control mb-3" name="regNo" value="${state.regNo}" readonly>
        <input class="form-control mb-3" type="number" name="semester" value="1" min="1" required>
        <input class="form-control mb-3" type="number" name="amount" placeholder="Amount" min="1" required>
        <select class="form-select mb-3" name="paymentMethod"><option>UPI</option><option>Card</option><option>Net Banking</option><option>Razorpay</option></select>
        <button class="btn btn-primary"><i class="fa-solid fa-credit-card me-2"></i>Pay Now</button>
    </form></div><div class="col-lg-7"><div class="glass panel p-4"><h2 class="h5">Payment History</h2>${paymentTable()}</div></div></div>`;
    bindFeeForm();
    await loadPayments(state.regNo);
}

function bindFeeForm() {
    const form = document.querySelector("#feeForm");
    if (!form) return;
    form.addEventListener("submit", async e => {
        e.preventDefault();
        const body = Object.fromEntries(new FormData(form).entries());
        body.semester = Number(body.semester);
        body.amount = Number(body.amount);
        try {
            const payment = await api("/api/fees/pay", { method: "POST", body: JSON.stringify(body) });
            toast(`Payment successful: ${payment.transactionId}`);
            await loadPayments(body.regNo);
        } catch (err) { toast(err.message, "danger"); }
    });
}

function paymentTable() {
    return `<div class="table-responsive"><table class="table align-middle"><thead><tr><th>Transaction</th><th>Amount</th><th>Method</th><th>Date</th><th>PDF</th></tr></thead><tbody id="paymentHistory"></tbody></table></div>`;
}

async function loadPayments(regNo) {
    const node = document.querySelector("#paymentHistory");
    if (!node || !regNo) return;
    const rows = await api(`/api/fees/payments/${regNo}`);
    node.innerHTML = rows.length ? rows.map(p => `<tr><td>${p.transactionId}</td><td>${rupees(p.amount)}</td><td>${p.paymentMethod}</td><td>${new Date(p.paymentDate).toLocaleString()}</td><td><button class="btn btn-sm btn-outline-primary" data-download="/api/fees/receipt/${p.paymentId}" data-filename="fee-receipt-${p.paymentId}.pdf"><i class="fa-solid fa-file-pdf"></i></button></td></tr>`).join("") : `<tr><td colspan="5" class="text-muted-portal">No payments yet</td></tr>`;
    bindDownloads(node);
}

async function studentHostel() {
    document.querySelector("#dashboardTitle").textContent = "Hostel Booking";
    document.querySelector("#dashboardSubtitle").textContent = "Select hostel, room number, sharing and mess preference";
    const hostels = await api("/api/hostel/list");
    content().innerHTML = `<div class="row g-4"><div class="col-lg-6"><form id="hostelForm" class="glass panel p-4">
        <h2 class="h5 mb-3">Book Hostel Room</h2>
        <input class="form-control mb-3" name="regNo" value="${state.regNo}" readonly>
        <select class="form-select mb-3" id="hostelId" name="hostelId" required><option value="">Hostel Name</option>${hostels.map(h => `<option value="${h.hostelId}">${h.hostelName}</option>`).join("")}</select>
        <select class="form-select mb-3" id="roomType" required><option value="">Room Type</option><option>1 Sharing AC</option><option>2 Sharing AC</option><option>3 Sharing Non AC</option><option>4 Sharing Non AC</option></select>
        <select class="form-select mb-3" name="roomId" id="roomId" required><option value="">Available Room Numbers</option></select>
        <div id="roomStatusList" class="room-status-list mb-3"></div>
        <select class="form-select mb-3" name="messType" required><option value="">Mess Type</option><option>Veg</option><option>Non Veg</option></select>
        <button class="btn btn-primary"><i class="fa-solid fa-bed me-2"></i>Book Room</button>
    </form></div><div class="col-lg-6"><div class="glass panel p-4"><h2 class="h5">Hostel Fee</h2><div id="hostelFeePreview" class="mt-3 text-muted-portal">Select a room to view hostel fee.</div></div><div class="glass panel p-4 mt-3" id="allocationPreview"></div></div></div>`;
    bindHostelForm();
    await renderStudentAllocation();
}

function bindHostelForm() {
    const form = document.querySelector("#hostelForm");
    const reloadRooms = async () => {
        const hostelId = document.querySelector("#hostelId").value;
        const roomType = document.querySelector("#roomType").value;
        const roomSelect = document.querySelector("#roomId");
        roomSelect.innerHTML = `<option value="">Available Room Numbers</option>`;
        if (!hostelId || !roomType) return;
        const rooms = await api(`/api/hostel/rooms?hostelId=${hostelId}&roomType=${encodeURIComponent(roomType)}`);
        roomSelect.innerHTML += rooms.length
            ? rooms.map(r => {
                const available = availableBeds(r);
                const capacity = Number(r.bedCapacity || capacityFromRoomType(r.roomType));
                const occupied = Number(r.occupiedBeds || 0);
                const status = available === 0 ? "Full" : occupied > 0 ? "Partially booked" : "Available";
                return `<option value="${r.roomId}" data-fee="${r.roomFee}" class="${roomStatusClass(r)}" ${available === 0 ? "disabled" : ""}>${r.roomNumber} - ${available}/${capacity} beds available (${status})</option>`;
            }).join("")
            : `<option value="">No rooms available</option>`;
        renderRoomStatusList(rooms);
    };
    ["#hostelId", "#roomType"].forEach(id => document.querySelector(id).addEventListener("change", reloadRooms));
    document.querySelector("#roomId").addEventListener("change", e => {
        const fee = Number(e.target.options[e.target.selectedIndex].dataset.fee || 0);
        document.querySelector("#hostelFeePreview").innerHTML = feeBreakdown(0, fee, 45000);
    });
    form.addEventListener("submit", async e => {
        e.preventDefault();
        const body = Object.fromEntries(new FormData(form).entries());
        body.roomId = Number(body.roomId);
        try {
            await api("/api/hostel/book", { method: "POST", body: JSON.stringify(body) });
            toast("Hostel room booked");
            await studentHostel();
        } catch (err) { toast(err.message, "danger"); }
    });
}

function availableBeds(room) {
    return Math.max(Number(room.bedCapacity || capacityFromRoomType(room.roomType)) - Number(room.occupiedBeds || 0), 0);
}

function capacityFromRoomType(roomType) {
    const first = String(roomType || "1").trim().charAt(0);
    return Number.isInteger(Number(first)) ? Number(first) || 1 : 1;
}

function roomStatusClass(room) {
    const available = availableBeds(room);
    const occupied = Number(room.occupiedBeds || 0);
    if (available === 0) return "room-full";
    if (occupied > 0) return "room-partial";
    return "room-open";
}

function renderRoomStatusList(rooms) {
    const node = document.querySelector("#roomStatusList");
    if (!node) return;
    if (!rooms.length) {
        node.innerHTML = `<div class="text-muted-portal small">No rooms found for this hostel and room type.</div>`;
        return;
    }
    node.innerHTML = rooms.map(room => {
        const available = availableBeds(room);
        const capacity = Number(room.bedCapacity || capacityFromRoomType(room.roomType));
        const occupied = Number(room.occupiedBeds || 0);
        const label = available === 0 ? "Full" : occupied > 0 ? `${available} bed${available === 1 ? "" : "s"} available` : "All beds available";
        return `<span class="room-pill ${roomStatusClass(room)}">${room.roomNumber}<small>${occupied}/${capacity} booked | ${label}</small></span>`;
    }).join("");
}

async function renderStudentAllocation() {
    const node = document.querySelector("#allocationPreview");
    if (!node) return;
    try {
        const [booking, fees] = await Promise.all([api(`/api/hostel/${state.regNo}`), api(`/api/fees/history/${state.regNo}`)]);
        const fee = fees[0] || {};
        node.innerHTML = `<h2 class="h5">Current Allocation</h2>
            <p class="mb-1"><strong>${booking.hostel.hostelName}</strong></p>
            <p class="mb-1">Room ${booking.roomNumber} | ${booking.room.roomType}</p>
            <p class="mb-1">Mess: ${booking.messType}</p>
            <p class="mb-1">Payment: ${badge(booking.hostelPaymentStatus || fee.paymentStatus)}</p>
            <p class="mb-0">Paid Amount: ${rupees(fee.paidFee)}</p>
            <button class="btn btn-sm btn-outline-primary mt-3" data-download="/api/hostel/receipt/${state.regNo}" data-filename="hostel-receipt-${state.regNo}.pdf"><i class="fa-solid fa-file-pdf me-1"></i>Receipt</button>`;
        bindDownloads(node);
    } catch {
        node.innerHTML = `<h2 class="h5">Current Allocation</h2><p class="text-muted-portal mb-0">No hostel booking yet.</p>`;
    }
}

async function studentProfile() {
    document.querySelector("#dashboardTitle").textContent = "Profile";
    document.querySelector("#dashboardSubtitle").textContent = "Student profile and hostel allocation";
    const [s, fees] = await Promise.all([api(`/api/students/${state.regNo}`), api(`/api/fees/history/${state.regNo}`)]);
    let hostelHtml = `<p class="text-muted-portal mb-0">No hostel allocated.</p>`;
    try {
        const b = await api(`/api/hostel/${state.regNo}`);
        hostelHtml = `<div class="row g-2">
            <div class="col-md-6">${profileLine("Hostel Name", b.hostel.hostelName)}</div>
            <div class="col-md-6">${profileLine("Room Number", b.roomNumber)}</div>
            <div class="col-md-6">${profileLine("Room Type", b.room.roomType)}</div>
            <div class="col-md-6">${profileLine("Mess Type", b.messType)}</div>
            <div class="col-md-6">${profileLine("Payment Status", badge(b.hostelPaymentStatus || fees[0]?.paymentStatus))}</div>
            <div class="col-md-6">${profileLine("Paid Amount", rupees(fees[0]?.paidFee || 0))}</div>
        </div>`;
    } catch {}
    content().innerHTML = `<div class="row g-4"><div class="col-lg-5"><div class="glass panel p-4">
        <h2 class="h4">${s.firstName} ${s.lastName}</h2><p class="text-muted-portal">${s.regNo} | ${s.department}</p>
        <p>${s.email}</p><p>${s.mobile}</p><p>${s.address || ""} ${s.city || ""} ${s.state || ""}</p>
        <button class="btn btn-primary" data-download="/api/documents/id-card/${s.regNo}" data-filename="id-card-${s.regNo}.pdf"><i class="fa-solid fa-id-card me-2"></i>ID Card</button>
    </div></div><div class="col-lg-7"><div class="glass panel p-4"><h2 class="h5">Hostel Details</h2>${hostelHtml}</div></div></div>`;
    bindDownloads(content());
}

function profileLine(label, value) {
    return `<div class="p-3 bg-white bg-opacity-50 rounded-2"><small class="text-muted-portal">${label}</small><div class="fw-semibold">${value || "-"}</div></div>`;
}

async function studentAnnouncements() {
    document.querySelector("#dashboardTitle").textContent = "Announcements";
    document.querySelector("#dashboardSubtitle").textContent = "University updates";
    const list = await api("/api/announcements");
    content().innerHTML = renderAnnouncements(list);
}

async function renderAdminView(view, silent = false) {
    try {
        if (!silent) loading();
        if (view === "analytics") return adminAnalytics();
        if (view === "students") return adminStudents();
        if (view === "courses") return adminCourses();
        if (view === "hostel") return adminHostel();
        if (view === "announcements") return adminAnnouncements();
    } catch (e) {
        toast(e.message, "danger");
    }
}

async function adminAnalytics() {
    const stats = await api("/api/admin/dashboard");
    document.querySelector("#dashboardTitle").textContent = "Admin Analytics";
    document.querySelector("#dashboardSubtitle").textContent = "Admissions, fee collection and hostel occupancy";
    content().innerHTML = `<div class="row g-3 mb-4">
        ${metric(["Students", stats.totalStudents, "fa-user-graduate"])}
        ${metric(["Courses", stats.totalCourses, "fa-book"])}
        ${metric(["Occupied Rooms", stats.occupiedRooms, "fa-bed"])}
        ${metric(["Fee Collected", rupees(stats.totalFeeCollected), "fa-indian-rupee-sign"])}
    </div><div class="row g-3"><div class="col-lg-7"><div class="glass panel p-4"><h2 class="h5">Department-wise Students</h2><canvas id="deptChart" height="120"></canvas></div></div><div class="col-lg-5"><div class="glass panel p-4"><h2 class="h5">Payment Status</h2><canvas id="payChart" height="120"></canvas></div></div></div>`;
    chart("deptChart", "bar", Object.keys(stats.departmentWiseStudents), Object.values(stats.departmentWiseStudents));
    chart("payChart", "doughnut", Object.keys(stats.paymentStatus), Object.values(stats.paymentStatus));
}

async function adminStudents(search = "") {
    document.querySelector("#dashboardTitle").textContent = "Student Records";
    document.querySelector("#dashboardSubtitle").textContent = "Approve or reject admissions";
    const data = await api(`/api/students?size=50&search=${encodeURIComponent(search)}`);
    content().innerHTML = `<div class="glass panel p-4"><div class="d-flex flex-wrap gap-2 justify-content-between align-items-center mb-3"><h2 class="h5 mb-0">Admissions</h2><input id="studentSearch" class="form-control" style="max-width:280px" placeholder="Search students" value="${search}"></div><div class="table-responsive"><table class="table align-middle"><thead><tr><th>Reg No</th><th>Name</th><th>Department</th><th>Mobile</th><th>Status</th><th>Action</th></tr></thead><tbody>${data.content.map(studentRow).join("")}</tbody></table></div></div>`;
    document.querySelector("#studentSearch").addEventListener("input", debounce(e => adminStudents(e.target.value), 350));
    content().querySelectorAll("[data-status]").forEach(btn => btn.addEventListener("click", async () => {
        await api(`/api/students/${btn.dataset.reg}/status`, { method: "PUT", body: JSON.stringify({ status: btn.dataset.status }) });
        toast(`Student ${btn.dataset.status.toLowerCase()}`);
        await adminStudents(document.querySelector("#studentSearch")?.value || "");
    }));
}

function studentRow(s) {
    return `<tr><td>${s.regNo}</td><td>${s.firstName} ${s.lastName}</td><td>${s.department}</td><td>${s.mobile}</td><td>${badge(s.status)}</td><td class="text-nowrap">
        <button class="btn btn-sm btn-success" data-reg="${s.regNo}" data-status="APPROVED"><i class="fa-solid fa-check"></i></button>
        <button class="btn btn-sm btn-outline-danger" data-reg="${s.regNo}" data-status="REJECTED"><i class="fa-solid fa-xmark"></i></button>
    </td></tr>`;
}

async function adminCourses() {
    const courses = await api("/api/courses");
    document.querySelector("#dashboardTitle").textContent = "Courses";
    document.querySelector("#dashboardSubtitle").textContent = "Fee structure and seats";
    content().innerHTML = `<div class="row g-3">${courses.map(courseCard).join("")}</div>`;
}

async function adminHostel() {
    document.querySelector("#dashboardTitle").textContent = "Hostel Allocations";
    document.querySelector("#dashboardSubtitle").textContent = "Occupied rooms, payment status and live availability";
    const [bookings, rooms] = await Promise.all([api("/api/hostel/allocations"), api("/api/hostel/rooms/all")]);
    content().innerHTML = `<div class="row g-3 mb-4">
        ${metric(["Occupied", rooms.filter(r => r.availabilityStatus === "OCCUPIED").length, "fa-bed"])}
        ${metric(["Available", rooms.filter(r => r.availabilityStatus === "AVAILABLE").length, "fa-door-open"])}
        ${metric(["Allocations", bookings.length, "fa-clipboard-check"])}
        ${metric(["Pending Hostel Fees", bookings.filter(b => b.hostelPaymentStatus !== "PAID").length, "fa-money-bill"])}
    </div><div class="glass panel p-4"><h2 class="h5">Allocation Details</h2><div class="table-responsive"><table class="table align-middle"><thead><tr><th>Student</th><th>Hostel</th><th>Room</th><th>Type</th><th>Mess</th><th>Payment</th></tr></thead><tbody>${bookings.map(b => `<tr><td>${b.student.regNo}<br><small>${b.student.firstName} ${b.student.lastName}</small></td><td>${b.hostel.hostelName}</td><td>${b.roomNumber}</td><td>${b.room.roomType}</td><td>${b.messType}</td><td>${badge(b.hostelPaymentStatus)}</td></tr>`).join("") || `<tr><td colspan="6" class="text-muted-portal">No allocations yet</td></tr>`}</tbody></table></div></div>`;
}

async function adminAnnouncements() {
    document.querySelector("#dashboardTitle").textContent = "Announcements";
    document.querySelector("#dashboardSubtitle").textContent = "Post and view university announcements";
    const list = await api("/api/announcements");
    content().innerHTML = `<div class="row g-4"><div class="col-lg-5"><form id="announcementForm" class="glass panel p-4"><h2 class="h5 mb-3">Post Announcement</h2><input class="form-control mb-3" name="title" placeholder="Title" required><textarea class="form-control mb-3" name="description" rows="5" placeholder="Description" required></textarea><button class="btn btn-primary">Post</button></form></div><div class="col-lg-7">${renderAnnouncements(list)}</div></div>`;
    document.querySelector("#announcementForm").addEventListener("submit", async e => {
        e.preventDefault();
        await api("/api/announcements", { method: "POST", body: JSON.stringify(Object.fromEntries(new FormData(e.target).entries())) });
        toast("Announcement posted");
        await adminAnnouncements();
    });
}

function renderAnnouncements(list) {
    return list.length ? list.map(a => `<div class="glass panel p-4 mb-3"><h3 class="h5">${a.title}</h3><p class="mb-1">${a.description}</p><small class="text-muted-portal">${new Date(a.postedDate).toLocaleString()}</small></div>`).join("") : `<div class="glass panel p-4 text-muted-portal">No announcements yet.</div>`;
}

function metric([label, value, icon]) {
    return `<div class="col-md-6 col-xl-3"><div class="glass metric"><i class="fa-solid ${icon} text-primary"></i><div class="fs-3 fw-bold mt-2">${value}</div><div class="text-muted-portal">${label}</div></div></div>`;
}

function chart(id, type, labels, data) {
    const canvas = document.getElementById(id);
    if (!canvas) return;
    new Chart(canvas, { type, data: { labels, datasets: [{ data, backgroundColor: ["#0d6efd", "#30c5ff", "#20c997", "#ffc107", "#dc3545", "#6f42c1"] }] }, options: { responsive: true } });
}

function badge(status) {
    const styles = { APPROVED: "success", PAID: "success", AVAILABLE: "success", PENDING: "warning", PARTIAL: "info", REJECTED: "danger", OCCUPIED: "secondary" };
    return `<span class="badge text-bg-${styles[status] || "secondary"}">${status || "-"}</span>`;
}

function bindDownloads(root = document) {
    root.querySelectorAll("[data-download]").forEach(button => {
        button.addEventListener("click", async () => {
            try {
                await downloadFile(button.dataset.download, button.dataset.filename || "document.pdf");
            } catch (err) {
                toast(err.message, "danger");
            }
        });
    });
}

function debounce(fn, wait) {
    let timeout;
    return (...args) => {
        clearTimeout(timeout);
        timeout = setTimeout(() => fn(...args), wait);
    };
}
