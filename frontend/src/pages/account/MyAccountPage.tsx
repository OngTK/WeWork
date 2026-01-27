import { useEffect, useMemo, useState } from "react";
import { Box, Button, Card, Divider, Input, Stack, Typography } from "@mui/joy";
import { useAuth } from "../../store/auth/AuthContext";
import { accountApi } from "../../api/accountApi";
import type { SexCode } from "../../store/auth/AuthContext"
import { Select, Option } from "@mui/joy";
import { useAppSnackbar } from "../../store/snackbar/SnackbarProvider";

type EditForm = {
    name: string;
    birthday: string;
    email: string;
    sex: SexCode;
};

export default function MyAccountPage() {
    const { account, refreshMe } = useAuth();
    const [editMode, setEditMode] = useState(false);
    const [saving, setSaving] = useState(false);

    const snackbar = useAppSnackbar();

    const initialForm: EditForm = useMemo(
        () => ({
            name: account?.name ?? "",
            birthday: account?.birthday ?? "",
            email: account?.email ?? "",
            sex: account?.sex ?? "O",
        }),
        [account]
    );

    const [form, setForm] = useState<EditForm>(initialForm);

    useEffect(() => {
        // account 변경(새로고침/재로그인) 시 폼 동기화
        setForm(initialForm);
    }, [initialForm]);

    if (!account) return null;

    function onChange<K extends keyof EditForm>(key: K, value: EditForm[K]) {
        setForm((prev) => ({ ...prev, [key]: value }));
    }

    function startEdit() {
        setForm(initialForm);
        setEditMode(true);
    }

    function cancelEdit() {
        setForm(initialForm);
        setEditMode(false);
    }

    async function save() {
        // 간단 검증(필요 시 강화)
        if (!form.name.trim()) return alert("이름을 입력해주세요.");
        if (!form.birthday) return alert("생년월일을 입력해주세요.");
        if (!form.email.trim()) return alert("이메일을 입력해주세요.");

        setSaving(true);
        try {
            await accountApi.updateMyInfo({
                name: form.name.trim(),
                birthday: form.birthday,
                email: form.email.trim(),
                sex: form.sex,
            });

            // 저장 후 최신 내정보 재조회 → 헤더 등 즉시 반영
            await refreshMe();
            setEditMode(false);
            // ✅ 성공 토스트 표시 (2초)
            snackbar.success("수정 성공했습니다.");
        } catch (e: any) {
            snackbar.error("수정에 실패했습니다. 서버 로그/응답을 확인해주세요.");
        } finally {
            setSaving(false);
        }
    }

    function sexLabel(sex?: SexCode) {
        if (sex === "M") return "남자";
        if (sex === "F") return "여자";
        return "무관"; // undefined 포함
    }

    return (
        <>
            <Box sx={{ width: "95%", mx: "auto" }}>
                <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
                    <Box>
                        <Typography level="h3">내 정보 관리</Typography>
                        <Typography level="body-sm" sx={{ color: "neutral.500", mt: 0.5 }}>
                            내 정보를 확인하고 수정할 수 있습니다.
                        </Typography>
                    </Box>

                    <Stack direction="row" spacing={1}>
                        {/* 비밀번호 수정: 추후 모달/컴포넌트 연결용 */}
                        <Button
                            variant="outlined"
                            onClick={() => {
                                snackbar.warn("비밀번호 수정 기능은 추후 연결 예정입니다.");
                            }}
                        >
                            비밀번호 수정
                        </Button>

                        {!editMode ? (
                            <Button onClick={startEdit}>수정하기</Button>
                        ) : (
                            <>
                                <Button variant="outlined" onClick={cancelEdit} disabled={saving}>
                                    취소
                                </Button>
                                <Button onClick={save} loading={saving}>
                                    저장
                                </Button>
                            </>
                        )}
                    </Stack>
                </Stack>

                <Card variant="outlined" sx={{ borderRadius: 16, p: 2.5 }}>
                    {/* 상단: 수정 불가 정보 */}
                    <Typography
                        level="title-md"
                        sx={{
                            mb: 1,
                            display: "flex",
                            justifyContent: "space-between",
                            alignItems: "center",
                        }}
                    >
                        <span>기본 정보</span>

                        <span style={{ color: "#868686", fontSize: "0.8rem" }}>
                            ※ 기본정보 수정은 관리자에게 문의하세요
                        </span>
                    </Typography>

                    <Stack spacing={1.5}>
                        <FieldRow label="사번(empId)">
                            <Input value={String(account.empId)} readOnly />
                        </FieldRow>

                        <FieldRow label="아이디(loginId)">
                            <Input value={account.loginId} readOnly />
                        </FieldRow>

                        <FieldRow label="직급(position)">
                            <Input value={account.position ?? "-"} readOnly />
                        </FieldRow>

                        <FieldRow label="부서명(deptName)">
                            <Input value={account.deptName ?? "-"} readOnly />
                        </FieldRow>
                    </Stack>

                    <Divider sx={{ my: 2.5 }} />

                    {/* 하단: 수정 가능 정보 */}
                    <Typography level="title-md" sx={{ mb: 1 }}>
                        수정 가능 정보
                    </Typography>

                    <Stack spacing={1.5}>
                        <FieldRow label="이름(name)">
                            <Input
                                value={editMode ? form.name : account.name}
                                readOnly={!editMode}
                                onChange={(e) => onChange("name", e.target.value)}
                            />
                        </FieldRow>

                        <FieldRow label="생년월일(birthday)">
                            <Input
                                type="date"
                                value={editMode ? form.birthday : account.birthday ?? ""}
                                readOnly={!editMode}
                                onChange={(e) => onChange("birthday", e.target.value)}
                            />
                        </FieldRow>

                        <FieldRow label="이메일(email)">
                            <Input
                                value={editMode ? form.email : account.email ?? ""}
                                readOnly={!editMode}
                                onChange={(e) => onChange("email", e.target.value)}
                            />
                        </FieldRow>
                        <FieldRow label="성별(sex)">
                            {editMode ? (
                                <Select
                                    value={form.sex}
                                    onChange={(_, v) => v && onChange("sex", v)}
                                    size="md"
                                >
                                    <Option value="M">남자</Option>
                                    <Option value="F">여자</Option>
                                    <Option value="O">무관</Option>
                                </Select>
                            ) : (
                                <Input value={sexLabel(account.sex)} readOnly />
                            )}
                        </FieldRow>
                    </Stack>

                    {/* roles는 표시하지 않음 (요구사항 반영) */}
                </Card>
            </Box>
        </>
    );
}

/** 공통 Row UI */
function FieldRow({ label, children }: { label: string; children: React.ReactNode }) {
    return (
        <Stack
            direction={{ xs: "column", md: "row" }}
            spacing={1}
            alignItems={{ xs: "stretch", md: "center" }}
        >
            <Box sx={{ width: { xs: "100%", md: 240 } }}>
                <Typography level="body-sm" sx={{ color: "neutral.600" }}>
                    {label}
                </Typography>
            </Box>
            <Box sx={{ flex: 1 }}>{children}</Box>
        </Stack>
    );
}
