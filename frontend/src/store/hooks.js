import { useDispatch, useSelector } from 'react-redux'

// Typed wrappers aren't needed in JSX — plain hooks work directly
export const useAppDispatch = () => useDispatch()
export const useAppSelector = useSelector