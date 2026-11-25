import type { MouseEvent } from 'react'
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination'

type Props = { page: number; totalPages: number; onPage: (page: number) => void }

// Pages affichées : première, dernière, et la page courante avec ses voisines
function pagesVisibles(page: number, total: number): (number | 'ellipse')[] {
  const garder = new Set([0, total - 1, page - 1, page, page + 1].filter((p) => p >= 0 && p < total))
  const triees = [...garder].sort((a, b) => a - b)
  const resultat: (number | 'ellipse')[] = []
  triees.forEach((p, i) => {
    if (i > 0 && p - triees[i - 1] > 1) resultat.push('ellipse')
    resultat.push(p)
  })
  return resultat
}

export function PaginationLivres({ page, totalPages, onPage }: Props) {
  if (totalPages <= 1) return null

  const aller = (p: number) => (e: MouseEvent) => {
    e.preventDefault()
    if (p >= 0 && p < totalPages && p !== page) onPage(p)
  }

  return (
    <Pagination className="mt-12">
      <PaginationContent>
        <PaginationItem>
          <PaginationPrevious
            href="#"
            text="Précédent"
            aria-label="Page précédente"
            aria-disabled={page === 0}
            className={page === 0 ? 'pointer-events-none opacity-40' : ''}
            onClick={aller(page - 1)}
          />
        </PaginationItem>
        {pagesVisibles(page, totalPages).map((p, i) =>
          p === 'ellipse' ? (
            <PaginationItem key={`e${i}`}>
              <PaginationEllipsis />
            </PaginationItem>
          ) : (
            <PaginationItem key={p}>
              <PaginationLink href="#" isActive={p === page} onClick={aller(p)} aria-label={`Page ${p + 1}`}>
                {p + 1}
              </PaginationLink>
            </PaginationItem>
          ),
        )}
        <PaginationItem>
          <PaginationNext
            href="#"
            text="Suivant"
            aria-label="Page suivante"
            aria-disabled={page >= totalPages - 1}
            className={page >= totalPages - 1 ? 'pointer-events-none opacity-40' : ''}
            onClick={aller(page + 1)}
          />
        </PaginationItem>
      </PaginationContent>
    </Pagination>
  )
}
