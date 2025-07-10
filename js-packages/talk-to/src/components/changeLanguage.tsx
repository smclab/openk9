import Box from '@mui/material/Box';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import MenuItem from '@mui/material/MenuItem';
import Select, { SelectChangeEvent } from '@mui/material/Select';
import { useTranslation } from 'react-i18next';

const languages: { value: string; label: string; }[] = [
    { value: "it", label: "Italiano" },
    { value: "en", label: "English" },
    { value: "fr", label: "Français" },
    { value: "es", label: "Español" }, 
  ];

export default function ChangeLanguage({}: {
    background?: string;
    minHeight?: string;
    color?: string;
}) {
    const { t, i18n } = useTranslation();
    const handleChange = (e: SelectChangeEvent<string>) => {
        i18n.changeLanguage(e?.target?.value);
    };

    return (
        <Box sx={{ minWidth: 40, maxHeight:10 }}>
            <FormControl size='small'>
                <InputLabel id="language-select-label">Language</InputLabel>
                <Select
                    labelId="language-select-label"
                    id="language-select"
                    value={i18n.language}
                    label="Language"
                    onChange={handleChange}
                    sx={{ borderRadius: "10px" }}
                >
                    {languages?.map((language) => (
                        <MenuItem key={language.value} value={language.value}>{language.label}</MenuItem>
                    ))}
                </Select>
            </FormControl>
        </Box>
    );
}
