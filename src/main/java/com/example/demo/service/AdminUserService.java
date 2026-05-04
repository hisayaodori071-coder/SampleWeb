package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.DepartmentEntity;
import com.example.demo.entity.EmploymentTypeEntity;
import com.example.demo.entity.GenderEntity;
import com.example.demo.entity.RoleEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.entity.UserRoleEntity;
import com.example.demo.form.AdminUserForm;
import com.example.demo.model.AdminUserListRow;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.EmploymentTypeRepository;
import com.example.demo.repository.GenderRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserRoleRepository;

@Service
public class AdminUserService {

    private static final int NOT_DELETED = 0;
    private static final int DELETED = 1;
    private static final String ACTIVE = "ACTIVE";
    private static final String RETIRED = "RETIRED";
    private static final DateTimeFormatter PASSWORD_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final UserRepository userRepository;
    private final GenderRepository genderRepository;
    private final DepartmentRepository departmentRepository;
    private final EmploymentTypeRepository employmentTypeRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public AdminUserService(
            UserRepository userRepository,
            GenderRepository genderRepository,
            DepartmentRepository departmentRepository,
            EmploymentTypeRepository employmentTypeRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.genderRepository = genderRepository;
        this.departmentRepository = departmentRepository;
        this.employmentTypeRepository = employmentTypeRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    public List<AdminUserListRow> getActiveUserRows() {
        List<UserEntity> users = userRepository.findByDeletedFlagAndUserStatusOrderByEmployeeIdAsc(NOT_DELETED, ACTIVE);

        Map<Long, String> departmentNameMap = new HashMap<>();
        for (DepartmentEntity department : getDepartments()) {
            departmentNameMap.put(department.getDepartmentId(), department.getDepartmentName());
        }

        List<AdminUserListRow> rows = new ArrayList<>();
        for (UserEntity user : users) {
            String fullName = safe(user.getLastName()) + " " + safe(user.getFirstName());
            String departmentName = departmentNameMap.getOrDefault(user.getDepartmentId(), "-");
            rows.add(new AdminUserListRow(
                    user.getUserId(),
                    user.getEmployeeId(),
                    fullName.trim(),
                    toUserStatusText(user.getUserStatus()),
                    departmentName,
                    user.getEmail()));
        }
        return rows;
    }

    public AdminUserForm createNewForm() {
        String nextEmployeeId = getNextEmployeeId();
        AdminUserForm form = new AdminUserForm();
        form.setEmployeeId(nextEmployeeId);
        form.setLoginId(nextEmployeeId);
        form.setRoleIds(getDefaultRoleIds());
        return form;
    }

    public Optional<AdminUserForm> getEditForm(Long userId) {
        return userRepository.findByUserIdAndDeletedFlagAndUserStatus(userId, NOT_DELETED, ACTIVE)
                .map(this::toFormForEdit);
    }

    public List<GenderEntity> getGenders() {
        return genderRepository.findAllByOrderByGenderIdAsc();
    }

    public List<DepartmentEntity> getDepartments() {
        return departmentRepository.findByDeletedFlagOrderBySortOrderAscDepartmentIdAsc(NOT_DELETED);
    }

    public List<EmploymentTypeEntity> getEmploymentTypes() {
        return employmentTypeRepository.findByDeletedFlagOrderBySortOrderAscEmploymentTypeIdAsc(NOT_DELETED);
    }

    public List<RoleEntity> getRoles() {
        return roleRepository.findByDeletedFlagOrderBySortOrderAscRoleIdAsc(NOT_DELETED);
    }

    public List<String> validateForCreate(AdminUserForm form) {
        List<String> errors = validateCommon(form, true);

        String employeeId = form.getEmployeeId();
        String loginId = form.getLoginId();

        if (hasText(employeeId) && userRepository.existsByEmployeeId(employeeId)) {
            errors.add("社員番号がすでに使用されています。画面を再読み込みしてから登録してください。");
        }
        if (hasText(loginId) && userRepository.existsByLoginId(loginId.trim())) {
            errors.add("ログインIDがすでに使用されています。");
        }
        if (hasText(form.getEmail()) && userRepository.existsByEmail(form.getEmail().trim())) {
            errors.add("メールアドレスがすでに使用されています。");
        }

        return errors;
    }

    public List<String> validateForUpdate(Long userId, AdminUserForm form) {
        List<String> errors = validateCommon(form, true);

        if (userRepository.findByUserIdAndDeletedFlagAndUserStatus(userId, NOT_DELETED, ACTIVE).isEmpty()) {
            errors.add("対象ユーザーが見つかりません。");
        }

        if (hasText(form.getLoginId())
                && userRepository.existsByLoginIdAndUserIdNot(form.getLoginId().trim(), userId)) {
            errors.add("ログインIDがすでに使用されています。");
        }

        if (hasText(form.getEmail())
                && userRepository.existsByEmailAndUserIdNot(form.getEmail().trim(), userId)) {
            errors.add("メールアドレスがすでに使用されています。");
        }

        return errors;
    }

    public List<String> validateForRetire(Long userId, String loginId, LocalDate retireDate) {
        List<String> errors = new ArrayList<>();

        Optional<UserEntity> optionalTargetUser = userRepository.findByUserIdAndDeletedFlagAndUserStatus(userId, NOT_DELETED, ACTIVE);
        if (optionalTargetUser.isEmpty()) {
            errors.add("対象ユーザーが見つかりません。");
            return errors;
        }

        UserEntity targetUser = optionalTargetUser.get();

        Long loginUserId = userRepository.findByLoginIdAndDeletedFlag(loginId, NOT_DELETED)
                .map(UserEntity::getUserId)
                .orElse(null);
        if (loginUserId != null && loginUserId.equals(userId)) {
            errors.add("ログイン中のユーザー自身は退職処理できません。");
        }

        if (retireDate == null) {
            errors.add("退職日を入力してください。");
            return errors;
        }

        if (targetUser.getHireDate() != null && retireDate.isBefore(targetUser.getHireDate())) {
            errors.add("退職日は入社日以降の日付を入力してください。");
        }

        if (retireDate.isAfter(LocalDate.now())) {
            errors.add("退職日は今日以前の日付を入力してください。");
        }

        return errors;
    }

    @Transactional
    public String createUser(AdminUserForm form) {
        String employeeId = getNextEmployeeId();
        LocalDateTime now = LocalDateTime.now();

        UserEntity user = new UserEntity();
        user.setEmployeeId(employeeId);
        user.setLoginId(employeeId);
        user.setPassword(resolvePassword(form));
        user.setLastName(trim(form.getLastName()));
        user.setFirstName(trim(form.getFirstName()));
        user.setLastNameKana(trim(form.getLastNameKana()));
        user.setFirstNameKana(trim(form.getFirstNameKana()));
        user.setBirthDate(form.getBirthDate());
        user.setEmail(trim(form.getEmail()));
        user.setGenderId(form.getGenderId());
        user.setDepartmentId(form.getDepartmentId());
        user.setEmploymentTypeId(form.getEmploymentTypeId());
        user.setUserStatus(ACTIVE);
        user.setHireDate(form.getHireDate());
        user.setRetireDate(form.getRetireDate());
        user.setSortOrder(0);
        user.setDeletedFlag(NOT_DELETED);
        user.setUpdatedAt(now);

        UserEntity savedUser = userRepository.save(user);
        replaceUserRoles(savedUser.getUserId(), form.getRoleIds(), now);

        return employeeId;
    }

    @Transactional
    public void updateUser(Long userId, AdminUserForm form) {
        UserEntity user = userRepository.findByUserIdAndDeletedFlagAndUserStatus(userId, NOT_DELETED, ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("対象ユーザーが見つかりません。"));

        user.setLastName(trim(form.getLastName()));
        user.setFirstName(trim(form.getFirstName()));
        user.setLastNameKana(trim(form.getLastNameKana()));
        user.setFirstNameKana(trim(form.getFirstNameKana()));
        user.setBirthDate(form.getBirthDate());

        // 編集画面ではログインIDも更新可能
        user.setLoginId(trim(form.getLoginId()));

        // 編集画面ではパスワードも更新可能
        user.setPassword(trim(form.getPassword()));

        user.setEmail(trim(form.getEmail()));
        user.setGenderId(form.getGenderId());
        user.setDepartmentId(form.getDepartmentId());
        user.setEmploymentTypeId(form.getEmploymentTypeId());
        user.setHireDate(form.getHireDate());
        user.setRetireDate(form.getRetireDate());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        replaceUserRoles(userId, form.getRoleIds(), LocalDateTime.now());
    }

    @Transactional
    public void retireUser(Long userId, LocalDate retireDate) {
        UserEntity user = userRepository.findByUserIdAndDeletedFlagAndUserStatus(userId, NOT_DELETED, ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("対象ユーザーが見つかりません。"));

        user.setRetireDate(retireDate);
        user.setUserStatus(RETIRED);
        user.setDeletedFlag(DELETED);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public String getNextEmployeeId() {
        int maxNumber = 0;
        for (String employeeId : userRepository.findAllEmployeeIds()) {
            if (employeeId != null && employeeId.matches("\\d+")) {
                try {
                    maxNumber = Math.max(maxNumber, Integer.parseInt(employeeId));
                } catch (NumberFormatException ignored) {
                    // 数値に変換できない社員番号は採番対象から除外する
                }
            }
        }
        return String.format("%06d", maxNumber + 1);
    }

    private AdminUserForm toFormForEdit(UserEntity user) {
        AdminUserForm form = new AdminUserForm();
        form.setUserId(user.getUserId());
        form.setEmployeeId(user.getEmployeeId());
        form.setLastName(user.getLastName());
        form.setFirstName(user.getFirstName());
        form.setLastNameKana(user.getLastNameKana());
        form.setFirstNameKana(user.getFirstNameKana());
        form.setBirthDate(user.getBirthDate());
        form.setLoginId(user.getLoginId());

        // 編集画面では現在のパスワードも表示して編集可能にする
        form.setPassword(user.getPassword());

        form.setEmail(user.getEmail());
        form.setGenderId(user.getGenderId());
        form.setDepartmentId(user.getDepartmentId());
        form.setEmploymentTypeId(user.getEmploymentTypeId());
        form.setHireDate(user.getHireDate());
        form.setRetireDate(user.getRetireDate());
        form.setRoleIds(getRoleIdsByUserId(user.getUserId()));
        return form;
    }

    private List<Long> getRoleIdsByUserId(Long userId) {
        List<Long> roleIds = new ArrayList<>();
        for (UserRoleEntity userRole : userRoleRepository.findByUserId(userId)) {
            roleIds.add(userRole.getRoleId());
        }
        return roleIds;
    }

    private List<Long> getDefaultRoleIds() {
        return roleRepository.findByRoleCodeAndDeletedFlag("USER", NOT_DELETED)
                .map(role -> Collections.singletonList(role.getRoleId()))
                .orElseGet(ArrayList::new);
    }

    private List<String> validateCommon(AdminUserForm form, boolean requirePassword) {
        List<String> errors = new ArrayList<>();

        if (!hasText(form.getEmployeeId())) {
            errors.add("社員番号が取得できません。画面を再読み込みしてください。");
        }
        if (!hasText(form.getLastName())) {
            errors.add("姓を入力してください。");
        }
        if (!hasText(form.getFirstName())) {
            errors.add("名を入力してください。");
        }
        if (!hasText(form.getLastNameKana())) {
            errors.add("姓カナを入力してください。");
        } else if (!isKatakana(form.getLastNameKana())) {
            errors.add("姓カナは全角カタカナで入力してください。");
        }

        if (!hasText(form.getFirstNameKana())) {
            errors.add("名カナを入力してください。");
        } else if (!isKatakana(form.getFirstNameKana())) {
            errors.add("名カナは全角カタカナで入力してください。");
        }
        if (form.getBirthDate() == null) {
            errors.add("生年月日を入力してください。");
        } else if (form.getBirthDate().isAfter(LocalDate.now())) {
            errors.add("生年月日は今日以前の日付を入力してください。");
        }
        if (!hasText(form.getLoginId())) {
            errors.add("ログインIDを入力してください。");
        }
        if (requirePassword && !hasText(form.getPassword())) {
            errors.add("パスワードを入力してください。");
        }
        if (!hasText(form.getEmail())) {
            errors.add("メールアドレスを入力してください。");
        } else if (!isValidEmail(form.getEmail())) {
            errors.add("メールアドレスの形式が正しくありません。");
        }
        if (form.getGenderId() == null) {
            errors.add("性別を選択してください。");
        }
        if (form.getDepartmentId() == null) {
            errors.add("部署を選択してください。");
        }
        if (form.getEmploymentTypeId() == null) {
            errors.add("雇用区分を選択してください。");
        }
        if (form.getHireDate() == null) {
            errors.add("入社日を入力してください。");
        }
        if (form.getHireDate() != null && form.getRetireDate() != null
                && form.getRetireDate().isBefore(form.getHireDate())) {
            errors.add("退職日は入社日以降の日付を入力してください。");
        }
        if (form.getRoleIds() == null || form.getRoleIds().isEmpty()) {
            errors.add("権限を1つ以上選択してください。");
        }

        return errors;
    }

    private void replaceUserRoles(Long userId, List<Long> roleIds, LocalDateTime now) {
        // 既存権限を削除
        userRoleRepository.deleteByUserId(userId);

        // delete をDBへ反映してから insert する
        userRoleRepository.flush();

        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }

        // 同じroleIdが複数送られてきても1回だけ登録する
        Set<Long> uniqueRoleIds = new HashSet<>(roleIds);

        for (Long roleId : uniqueRoleIds) {
            UserRoleEntity userRole = new UserRoleEntity();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRole.setCreatedAt(now);
            userRole.setUpdatedAt(now);
            userRoleRepository.save(userRole);
        }
    }

    private String resolvePassword(AdminUserForm form) {
        if (hasText(form.getPassword())) {
            return form.getPassword().trim();
        }
        if (form.getBirthDate() != null) {
            return form.getBirthDate().format(PASSWORD_DATE_FORMAT);
        }
        return "";
    }

    private String toUserStatusText(String userStatus) {
        if (ACTIVE.equals(userStatus)) {
            return "有効";
        }
        if (RETIRED.equals(userStatus)) {
            return "退職済";
        }
        if ("SUSPENDED".equals(userStatus)) {
            return "停止中";
        }
        return safe(userStatus);
    }

    private boolean isValidEmail(String email) {
        String value = email == null ? "" : email.trim();
        return value.contains("@") && value.contains(".");
    }
    
    private boolean isKatakana(String value) {
        if (value == null) {
            return false;
        }
        return value.trim().matches("^[ァ-ヶー　\\s]+$");
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
