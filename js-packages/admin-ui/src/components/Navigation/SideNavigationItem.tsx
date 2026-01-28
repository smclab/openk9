/*
* Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
import React from "react";
import { NavLink } from "react-router-dom";
import { ListItem, ListItemText, ListItemButton, Collapse, List, useTheme } from "@mui/material";
import ExpandLess from '@mui/icons-material/ExpandLess';
import ExpandMore from '@mui/icons-material/ExpandMore';
import ArrowRightIcon from '@mui/icons-material/ArrowRight';
import { MenuItem } from "./types";
import { useSideNavigation } from "../sideNavigationContext";

interface SideNavigationItemProps {
  item: MenuItem;
  level?: number;
}

export function SideNavigationItem({ item, level = 0 }: SideNavigationItemProps) {
  const theme = useTheme();
  const { navigation, changaSideNavigation } = useSideNavigation();
  const [open, setOpen] = React.useState(false);

  const hasActiveChild = (menuItem: MenuItem): boolean => {
    if (menuItem.value === navigation) return true;
    if (menuItem.children) {
      return menuItem.children.some(child => hasActiveChild(child));
    }
    return false;
  };

  const isSelect = navigation === item.value;
  const isActiveParent = item.isGroup && hasActiveChild(item);

  const handleClick = () => {
    if (item.isGroup) {
      setOpen(!open);
    } else {
      changaSideNavigation(item.value);
    }
  };

  return (
    <>
      <ListItem disablePadding>
        {item.isGroup ? (
          <ListItemButton
            onClick={handleClick}
            sx={{
              pl: 2 + level * 2,
              minHeight: '36px',
              py: 0.75,
              borderRadius: '8px',
              mb: 0.5,
              '&:hover': {
                backgroundColor: 'rgba(255, 0, 0, 0.08)',
              },
              position: 'relative',
              ...(isActiveParent && !open && {
                '&::before': {
                  content: '""',
                  position: 'absolute',
                  left: '12px',
                  top: '50%',
                  transform: 'translateY(-50%)',
                  display: 'flex',
                  alignItems: 'center',
                  color: theme.palette.primary.main
                }
              })
            }}
          >
            {isActiveParent && !open && (
              <ArrowRightIcon 
                sx={{ 
                  position: 'absolute',
                  left: '-5px',
                  top: '50%',
                  transform: 'translateY(-50%)',
                  fontSize: '20px',
                  color: theme.palette.primary.main
                }} 
              />
            )}
            <ListItemText 
              primary={item.label}
              primaryTypographyProps={{ 
                fontSize: '0.9rem',
                fontWeight: isActiveParent && !open ? 'bold' : 'normal'
              }}
              sx={{
                margin: 0,
                ...(isActiveParent && !open && {
                  color: theme.palette.primary.main
                })
              }}
            />
            {open ? <ExpandLess /> : <ExpandMore />}
          </ListItemButton>
        ) : (
          <ListItemButton
            component={NavLink}
            to={item.path || ''}
            onClick={handleClick}
            sx={{
              pl: 2 + level * 2,
              minHeight: '36px',
              py: 0.75,
              borderRadius: '8px',
              mb: 0.5,
              ...(isSelect && {
                backgroundColor: theme.palette.primary.main,
                color: theme.palette.primary.contrastText,
              }),
              '&:hover': {
                backgroundColor: isSelect ? theme.palette.primary.main : 'rgba(255, 0, 0, 0.08)',
              }
            }}
          >
            <ListItemText
              primary={item.label}
              primaryTypographyProps={{ 
                fontSize: '0.9rem',
                fontWeight: isSelect ? 'bold' : 'normal',
              }}
              sx={{
                margin: 0,
                padding: 0
              }}
            />
          </ListItemButton>
        )}
      </ListItem>
      
      {item.isGroup && (
        <Collapse in={open} timeout="auto" unmountOnExit>
          <List component="div" disablePadding>
            {item.children?.map((child, index) => (
              <SideNavigationItem key={index} item={child} level={level + 1} />
            ))}
          </List>
        </Collapse>
      )}
    </>
  );
} 
