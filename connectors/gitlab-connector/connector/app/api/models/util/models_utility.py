from typing import Literal


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