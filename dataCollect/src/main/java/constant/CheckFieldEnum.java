package constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CheckFieldEnum {
    // 格式：占位符key | 实体取值表达式(简易路径) | 匹配值
    SAFE_MODEL_SYSTEM("safe_model_system", "safe.model", "系统控制"),
    SAFE_MODEL_OTHER("safe_model_other", "safe.model", "other"),
    // 在这里继续追加所有复选框字段，一行一个
    CHECK_TYPE1("check_type1", "safe.type", "类型A"),
    CHECK_TYPE2("check_type2", "safe.type", "类型B");

    /** 模板占位符对应的 map key */
    private final String mapKey;
    /** 实体属性路径 */
    private final String fieldPath;
    /** 判定匹配值 */
    private final String matchValue;
}

