from abc import ABC
from typing import Any, Literal, get_args


BoolFilter = Literal['True', 'False', 'NoFilter']


ProjectMinAccessLevelStrings = Literal[
    'NoFilter',
    'MinimalAccess', 'Guest', 'Planner', 'Reporter', 
    'SecurityManager', 'Developer', 'Maintainer', 'Owner'
]

PROJECT_MIN_ACCESS_LEVEL_MAP: dict[ProjectMinAccessLevelStrings, int] = {
    'MinimalAccess': 5,
    'Guest': 10,
    'Planner': 15,
    'Reporter': 20,
    'SecurityManager': 25,
    'Developer': 30,
    'Maintainer': 40,
    'Owner': 50
}



class FilterComposite(ABC):
    @classmethod
    def parse_value(cls, value: Any) -> Any:
        if value in get_args(BoolFilter):
            return value == 'True'
        return value
        
    @classmethod
    def compose_params(cls, filters: dict[str, Any]) -> dict[str, Any]:
        return {
            name: cls.parse_value(value)
            for name, value in filters.items()
            if value != 'NoFilter'
        }